package com.app.rubicon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.rubicon.pojo.WaterOrder;
import com.app.rubicon.pojo.WaterOrderStatus;

public interface WaterOrderRepository extends JpaRepository<WaterOrder, Integer> {

	// fetches the list of all water orders for a particular farmId
	List<WaterOrder> findByFarmId(String farmId);
	
	// fetches the list of "Requested" and "InProgress" water orders for a particular farmId
	@Query("SELECT wo FROM WaterOrder wo WHERE wo.farmId=:fid AND wo.status IN (:s1, :s2)")
	List<WaterOrder> fetchOrdersByFarmIdAndStatus(@Param("fid") String farmId, @Param("s1") WaterOrderStatus status1,
			@Param("s2") WaterOrderStatus status2);
}
