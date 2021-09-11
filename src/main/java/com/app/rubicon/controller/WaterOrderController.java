package com.app.rubicon.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.rubicon.pojo.WaterOrder;
import com.app.rubicon.pojo.WaterOrderStatus;
import com.app.rubicon.repository.WaterOrderRepository;

@RestController
@RequestMapping("/order")
public class WaterOrderController {

	Map<Integer, Timer> timers = new TreeMap<>(); // a map with key : orderId and value : timer object reference

	@Autowired
	private WaterOrderRepository waterOrderRepo;

	// for changing order status of the given order to the given status
	private void changeOrderStatus(WaterOrder order, WaterOrderStatus status) {
		order.setStatus(status);
		waterOrderRepo.save(order);
	}

	// scheduling tasks for future execution in a background thread
	private void scheduleTasks(Timer timer, WaterOrder order) {
		// at starting of order delivery
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				changeOrderStatus(order, WaterOrderStatus.InProgress);
				System.out.println("Water delivery to farm " + order.getFarmId() + " started");
			}
		}, Timestamp.valueOf(order.getStartTimestamp()));

		// at completion of order delivery
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				changeOrderStatus(order, WaterOrderStatus.Delivered);
				System.out.println("Water delivery to farm " + order.getFarmId() + " stopped");
			}
		}, Timestamp.valueOf(order.getStartTimestamp().plusHours(order.getDurationHours())));
	}

//	@GetMapping()
//	public String testing() {
//		return "working just fine...";
//	}

	// @RequestBody : for auto unmarshalling by Spring Boot
	@PostMapping("/new")
	public ResponseEntity<?> generateNewOrder(@RequestBody WaterOrder waterOrder) {
//		System.out.println("Reached....Creating new order...");

		try {
			// e.g If current time is 14:15 on 11-09-2021. Then, new order's start time can
			// only be after 15:15 on 11-09-2021.
			if (waterOrder.getStartTimestamp().isBefore(LocalDateTime.now().plusHours(1)))
				throw new RuntimeException("Order has to be placed atleast 1 hour in advance");

			// only an order which is either in "Requested" or "InProgress" state, will
			// participate in checking of overlapping with new order being generated
			List<WaterOrder> waterOrders = waterOrderRepo.fetchOrdersByFarmIdAndStatus(waterOrder.getFarmId(),
					WaterOrderStatus.Requested, WaterOrderStatus.InProgress);

			waterOrders.forEach(order -> {
				// checking condition of overlapping orders
				if (waterOrder.getStartTimestamp()
						.isBefore(order.getStartTimestamp().plusHours(order.getDurationHours()))
						&& waterOrder.getStartTimestamp().plusHours(waterOrder.getDurationHours())
								.isAfter(order.getStartTimestamp()))
					throw new RuntimeException("Overlapping of orders");
			});
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); // 409
		}

		waterOrder.setStatus(WaterOrderStatus.Requested);
		WaterOrder order = waterOrderRepo.save(waterOrder);
		System.out.println("New water order for farm " + order.getFarmId() + " created");

		// creating a new Timer for the new order
		Timer timer = new Timer();
		timers.put(order.getId(), timer);
		// scheduling tasks for logging information and changing order status on start
		// of order delivery and completion of order delivery
		scheduleTasks(timer, order);

		return new ResponseEntity<>(order, HttpStatus.CREATED); // 201
	}

	// cancelling the order with the given orderId
	@DeleteMapping("/cancel/{orderId}")
	public ResponseEntity<?> cancelOrder(@PathVariable int orderId) {

		Optional<WaterOrder> waterOrder = waterOrderRepo.findById(orderId);

		// only order which is in "Requested" state can be cancelled.
		if (waterOrder.isPresent()) {

			WaterOrder order = waterOrder.get();

			if (order.getStatus().equals(WaterOrderStatus.Requested)) {
				changeOrderStatus(order, WaterOrderStatus.Cancelled); // changing order status
				// cancelling the scheduled tasks and removing the timer corresponding to this
				// order
				timers.get(order.getId()).cancel();
				timers.remove(order.getId());
				System.out.println("Order for farm " + order.getFarmId() + " scheduled at " + order.getStartTimestamp()
						+ " has been cancelled");
				return new ResponseEntity<>(HttpStatus.OK); // 200
			} else if (order.getStatus().equals(WaterOrderStatus.InProgress)) {
				return new ResponseEntity<>(
						"Order delivery is in progress...cancellation is not possible at this moment",
						HttpStatus.CONFLICT);
			} else if (order.getStatus().equals(WaterOrderStatus.Delivered)) {
				return new ResponseEntity<>("Order has already been delivered", HttpStatus.CONFLICT);
			} else {
				return new ResponseEntity<>("Order was already cancelled", HttpStatus.OK);
			}

		}
		
		// An order with this orderId is not found
		return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
	}

	// fetch all orders for a particular farmId
	@GetMapping("/all/{farmId}")
	public ResponseEntity<?> fetchAllOrders(@PathVariable String farmId) {
		List<WaterOrder> waterOrders = waterOrderRepo.findByFarmId(farmId);

		if (waterOrders.isEmpty()) // there are no orders for this farmId
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		return new ResponseEntity<>(waterOrders, HttpStatus.OK);
	}

}
