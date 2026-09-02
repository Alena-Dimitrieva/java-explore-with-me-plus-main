package ru.practicum.ewm.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Location implements Serializable {
	Float lat;
	Float lon;
}
