import org.junit.jupiter.api.Test;
import model.EventGroup;
import static org.junit.jupiter.api.Assertions.*;

public class EventGroupTest {
    @Test
    public void testAddOptionNoDuplicates() {
        EventGroup group = new EventGroup("Concert");
        group.addOption("Hall1", "Mon");
        group.addOption("Hall1", "Mon");
        assertEquals("Hall1", group.getVenuesDisplay());
        assertEquals("Hall1 - Mon", group.getOptionsDisplay());
    }

    @Test
    public void titleIsStored() {
        EventGroup group = new EventGroup("Concert");
        assertEquals("Concert", group.getTitle());
    }

    @Test
    public void addingNullVenueResultsInLiteralNull() {
        EventGroup group = new EventGroup("Concert");
        group.addOption(null, "Tue");
        assertEquals("null", group.getVenuesDisplay());
    }

    @Test
    public void emptyGroupDisplaysEmptyStrings() {
        EventGroup group = new EventGroup("Concert");
        assertEquals("", group.getVenuesDisplay());
        assertEquals("", group.getOptionsDisplay());
    }
}