# Order Processing System
chmod +x restart.sh
./restart.sh

# Should show all services as "Up"
docker compose ps

# Check for "partitions assigned" messages
docker compose logs order-service | grep "partitions assigned"
docker compose logs inventory-service | grep "partitions assigned"
docker compose logs payment-service | grep "partitions assigned"

**Success looks like:**
order-service: partitions assigned: [payment-success-0, payment-failed-0]
inventory-service: partitions assigned: [order-created-0, payment-failed-0]
payment-service: partitions assigned: [inventory-reserved-0]

**Step 1: Create an order**
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"customerName":"Idempotency Test","totalAmount":100.00}'

docker compose logs inventory-service | tail -20

**Expected output:**
```
📥 Received OrderCreatedEvent: OrderCreatedEvent(eventId=abc-123-xyz, orderId=1, ...)
🔒 Reserving inventory for order: 1
✅ Inventory reserved for order: 1
✅ Marked event abc-123-xyz as processed
```

**Step 2: Check the database**
# Query processed_events table
docker exec order-processing-system-postgres-1 \
psql -U admin -d inventory_db \
-c "SELECT * FROM processed_events;"

**You should see:**
```

              event_id               |     event_type      |      processed_at       | consumer_service
-------------------------------------+---------------------+-------------------------+------------------
abc-123-xyz...                      | OrderCreatedEvent   | 2026-01-20 03:30:15.123 | inventory-service
```
**State Machine Validation**
Valid Order States:
PENDING → PROCESSING → CONFIRMED ✅
PENDING → PROCESSING → FAILED ✅
PENDING → CANCELLED ✅
Invalid:

PENDING → CONFIRMED ❌ (must go through PROCESSING)
CONFIRMED → PROCESSING ❌ (can't go backwards)
FAILED → CONFIRMED ❌ (can't resurrect failed orders)