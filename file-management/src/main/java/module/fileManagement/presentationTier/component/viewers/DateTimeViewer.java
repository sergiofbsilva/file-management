package module.fileManagement.presentationTier.component.viewers;

import org.joda.time.DateTime;

import com.vaadin.ui.Label;

public class DateTimeViewer extends Label {

    private String formatString;

    public DateTimeViewer() {
        this("dd/MM/yyyy HH:mm");
    }

    public DateTimeViewer(String format) {
        this.formatString = format;
    }

    @Override
    public String toString() {
        final DateTime datetime = (DateTime) getPropertyDataSource().getValue();
        return datetime != null ? datetime.toString(formatString) : "-";
    }

}
