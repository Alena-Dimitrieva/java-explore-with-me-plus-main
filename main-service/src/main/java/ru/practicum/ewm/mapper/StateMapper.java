package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.UserStateAction;

@Component
public class StateMapper {

	public static EventState mapUserEventAction(UserStateAction action) {
		return switch (action) {
			case CANCEL_REVIEW -> EventState.CANCELED;
			case SEND_TO_REVIEW -> EventState.PENDING;
			case null, default -> null;
		};
	}

	public static EventState mapAdminEventAction(UserStateAction action) {
		return switch (action) {
			case PUBLISH_EVENT -> EventState.PUBLISHED;
			case REJECT_EVENT -> EventState.CANCELED;
			case null, default -> null;
		};
	}
}
