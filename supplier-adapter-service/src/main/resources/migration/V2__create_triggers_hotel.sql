-- Create a function to handle hotel outbox events
CREATE OR REPLACE FUNCTION public.handle_hotel_outbox_event()
RETURNS TRIGGER AS $$
DECLARE
event_type TEXT;
event_payload JSONB;
BEGIN
    -- Determine event type based on operation
CASE TG_OP
        WHEN 'INSERT' THEN
            event_type := 'CREATED';
            event_payload := row_to_json(NEW)::jsonb;
WHEN 'UPDATE' THEN
            event_type := 'UPDATED';
            event_payload := row_to_json(NEW)::jsonb;
WHEN 'DELETE' THEN
            event_type := 'DELETED';
            event_payload := row_to_json(OLD)::jsonb;
END CASE;

-- Insert into outbox_events table atomically
INSERT INTO public.hotel_outbox_events (
    id, aggregate_type, type, payload, created_at
) VALUES (
             gen_random_uuid(), 'HOTEL_POLLED', event_type,
             event_payload, CURRENT_TIMESTAMP
         );
RETURN NULL;
END;
$$ LANGUAGE plpgsql;
-- Create triggers on hotel and category tables
DROP TRIGGER IF EXISTS hotel_outbox_trigger ON public.hotel_polled;
CREATE TRIGGER hotel_outbox_trigger
    AFTER INSERT OR UPDATE OR DELETE ON public.hotel_polled
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_hotel_outbox_event();