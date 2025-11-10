-- Create a function to handle movie outbox events
CREATE OR REPLACE FUNCTION public.handle_movie_outbox_event()
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
INSERT INTO public.outbox_events (
    id, aggregate_type, type, payload, created_at
) VALUES (
             gen_random_uuid(), 'MOVIE', event_type,
             event_payload, CURRENT_TIMESTAMP
         );
RETURN NULL;
END;
$$ LANGUAGE plpgsql;
-- Create triggers on movie and category tables
CREATE TRIGGER movie_outbox_trigger
    AFTER INSERT OR UPDATE OR DELETE ON public.movie
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_movie_outbox_event();