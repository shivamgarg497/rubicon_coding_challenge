package com.app.rubicon.pojo;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "water_order")
public class WaterOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // analogous to "auto_increment" in MySQL
	private Integer id;

	@Column(name = "farm_id", nullable = false, length = 10)
	private String farmId;

	@Column(name = "start_timestamp", nullable = false)
	private LocalDateTime startTimestamp;

	@Column(nullable = false)
	private int durationHours;

	@Enumerated(value = EnumType.STRING) // so that the corresponding column in database table will have varchar type
											// and will store name of Enum constants instead of ordinal
	@Column(nullable = false, length = 10)
	private WaterOrderStatus status;

	public WaterOrder() {
		// TODO Auto-generated constructor stub..
	}

	public WaterOrder(String farmId, LocalDateTime startTimestamp, int durationHours, WaterOrderStatus status) {
		super();
		this.farmId = farmId;
		this.startTimestamp = startTimestamp;
		this.durationHours = durationHours;
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFarmId() {
		return farmId;
	}

	public void setFarmId(String farmId) {
		this.farmId = farmId;
	}

	public LocalDateTime getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(LocalDateTime startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public long getDurationHours() {
		return durationHours;
	}

	public void setDurationHours(int durationHours) {
		this.durationHours = durationHours;
	}

	public WaterOrderStatus getStatus() {
		return status;
	}

	public void setStatus(WaterOrderStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "WaterOrder [id=" + id + ", farmId=" + farmId + ", startTimestamp=" + startTimestamp + ", duration="
				+ durationHours + ", status=" + status + "]";
	}

}
