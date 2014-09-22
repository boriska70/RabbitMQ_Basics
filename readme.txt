Based on http://www.rabbitmq.com/getstarted.html
Examples 5_1-5_3 are different variations for Topics

1. Basic "Hello wold" example

2. Work queues
- one and only consumer receives the message
- round robin dispatching: if long tasks sent (argument with many dots)
- message acknowledgment: ack is not sent immediately, so the message is not immediately removed but kept until processing finish
- message durability: every message that sent with persistent property is saved to disk, if the queue is durable
- fair dispatching: do not dispatch a new message to a consumer until it has processed and acknowledged the previous one.
  If no free consumers, keep the messages in the queue until one of consumers becomes ready

3. Publish/Subscribe
- All connected consumers receieve all messages
- Messages sent to exchange; queues only created when consumers connect (per consumer)
- In this example - messages discarded, if no queues exist (i.e., no consumers connected)

4. Routing (single criteria binding for pub/sub)
- All connected consumers receieve all messages
- Single criteria but multiple binding allowed
- Pub/Sub similar to the previous one but bound separately for routing key hello and bye, i.e., double bounding
- Still no message acknowledges and durability

5. Topic (pub/sub with multiple criteria)
- All connected consumers receive all messages (no queue share between customer)
- Multiple criteria and multiple binding
- Not receiving the messages that came before it started
- Special characters: * (exactly one word) or # (zero or more words)
- Each consumer receives the message only once, even if it fits multiple bindings
- Still no message acknowledges and durability

5.1. Topic - more complex one (unnecessary complexity, actually)
- All connected consumers receive all messages (no queue share between customer)
- Multiple criteria and multiple binding
- Not receiving the messages that came before it started
- Sender does not create any queue
- Each consumer receives the message only once, even if it fits multiple bindings
- exchange2exchange binding
- Explicit message acknowledge

5.2 Mix of topic and queue
- Only 1 consumer receive messages since all of them share the same queue (by name)
- Consumer receive the messages that came before it started, since the queue is durable. Cool, but not so good idea actually (see ANTI-PATTERN comment below)
- Sender creates the durable queues that used by the consumer then
- exchange2exchange binding

5.3 Supports everything (see below) BUT DO NOT USE IT (see below again)
- All consumers receive messages
- Consumers receive the messages that came before it started
- Each consumer declares queue with unique name for itself when starts
- exchange2exchange
- Sender declares queue with unique name for all consumers
- Sender and consumers are aware of number of consumers - ANTI-PATTERN, because:
     - sender and consumer are aware of each other (kind of - via queue name and number of consumers)
     - if consumer is down for a long time, its queue will become large