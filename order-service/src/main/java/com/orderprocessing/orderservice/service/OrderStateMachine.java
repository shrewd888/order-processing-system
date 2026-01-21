package com.orderprocessing.orderservice.service;

import com.orderprocessing.orderservice.model.OrderState;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class OrderStateMachine {

    // Define valid state transitions
    private static final Map<OrderState, Set<OrderState>> VALID_TRANSITIONS = Map.of(
            OrderState.PENDING, Set.of(OrderState.PROCESSING, OrderState.CANCELLED),
            OrderState.PROCESSING, Set.of(OrderState.CONFIRMED, OrderState.FAILED),
            OrderState.CONFIRMED, Set.of(),  // Terminal state
            OrderState.FAILED, Set.of(),     // Terminal state
            OrderState.CANCELLED, Set.of()   // Terminal state
    );

    /**
     * Check if state transition is valid
     */
    public boolean canTransition(OrderState from, OrderState to) {
        if (from == null || to == null) {
            log.error("Invalid state: from={}, to={}", from, to);
            return false;
        }

        Set<OrderState> allowedTransitions = VALID_TRANSITIONS.get(from);
        boolean isValid = allowedTransitions != null && allowedTransitions.contains(to);

        if (!isValid) {
            log.warn("❌ Invalid state transition: {} → {}", from, to);
        } else {
            log.info("✅ Valid state transition: {} → {}", from, to);
        }

        return isValid;
    }

    /**
     * Validate and transition state
     * @throws IllegalStateException if transition is invalid
     */
    public OrderState transition(OrderState from, OrderState to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s", from, to)
            );
        }
        return to;
    }

    /**
     * Get all valid next states from current state
     */
    public Set<OrderState> getValidNextStates(OrderState currentState) {
        return VALID_TRANSITIONS.getOrDefault(currentState, Set.of());
    }
}