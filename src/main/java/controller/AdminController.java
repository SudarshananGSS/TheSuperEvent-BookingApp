package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import model.*;
import service.EventService;
import utils.NavigationManager;
import utils.Result;
import java.util.List;

/**
 * Controller for the admin dashboard. Allows event management such as adding, modifying, enabling/disabling and deleting events.
 */
public class AdminController {
    @FXML private TableView<Event> groupEventsTable;
    @FXML private TableColumn<Event, String> detailTitleCol, detailVenueCol, detailDayCol;
    @FXML private TableColumn<Event, Double> detailPriceCol;
    @FXML private TableColumn<Event, Integer> detailTotalCol, detailSoldCol;
    @FXML private TableColumn<Event, Boolean> detailActiveCol;

    @FXML private TextField eventNameField, venueField, priceField, capacityField;
    @FXML private ChoiceBox<String> dayChoiceBox;

    @FXML private TableView<EventGroup> eventGroupTable;
    @FXML private TableColumn<EventGroup, String> groupTitleCol, groupVenuesCol, groupOptionsCol;
    @FXML private ChoiceBox<String> optionChoiceBox;

    @FXML private Label statusLabel;
    @FXML private Button addButton, modifyButton, deleteButton, enableButton, disableButton, logoutButton, viewAllOrdersButton;

    private EventService eventService;
    private PauseTransition hideMessage;

    public AdminController() {}

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
        loadGroupedEvents();
        if (eventGroupTable.getItems().isEmpty()) {
            statusLabel.setText("No grouped events found.");
        } else {
            statusLabel.setText("Grouped events loaded.");
            loadEventForSelectedOption();
        }
    }


    @FXML
    public void initialize() {
        setupDetailTable();
        dayChoiceBox.setItems(FXCollections.observableArrayList(
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));

        eventGroupTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                optionChoiceBox.setItems(FXCollections.observableArrayList(newSel.getOptionList()));
                if (!newSel.getOptionList().isEmpty()) {
                    optionChoiceBox.setValue(newSel.getOptionList().get(0));
                    updateSelectedOption(newSel.getTitle(), optionChoiceBox.getValue());
                } else {
                    groupEventsTable.setItems(FXCollections.observableArrayList());
                }
                loadEventForOption(newSel.getTitle(), optionChoiceBox.getValue());
            } else {
                groupEventsTable.setItems(FXCollections.observableArrayList());
            }
        });

        optionChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldOpt, newOpt) ->
                updateSelectedOptionForCurrentGroup(newOpt));
        optionChoiceBox.setOnAction(e -> updateSelectedOptionForCurrentGroup(optionChoiceBox.getValue()));
    }

    private void loadGroupedEvents() {
        String selectedTitle = null;
        EventGroup sel = eventGroupTable.getSelectionModel().getSelectedItem();
        if (sel != null) selectedTitle = sel.getTitle();

        List<EventGroup> groups = eventService.getGroupedEvents();
        eventGroupTable.setItems(FXCollections.observableArrayList(groups));
        groupTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        groupVenuesCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVenuesDisplay()));
        groupOptionsCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOptionsDisplay()));

        if (groups.isEmpty()) {
            statusLabel.setText("No grouped events found.");
            groupEventsTable.setItems(FXCollections.observableArrayList());
        } else {
            if (selectedTitle != null) {
                for (EventGroup g : groups) {
                    if (g.getTitle().equals(selectedTitle)) {
                        eventGroupTable.getSelectionModel().select(g);
                        break;
                    }
                }
            } else {
                eventGroupTable.getSelectionModel().selectFirst();
            }
            loadEventForSelectedOption();
        }
    }

    @FXML private void handleAddEvent() {
        try {
            String title = eventNameField.getText().trim();
            String venue = venueField.getText().trim();
            String day = dayChoiceBox.getValue();
            double price = Double.parseDouble(priceField.getText().trim());
            int capacity = Integer.parseInt(capacityField.getText().trim());

            Event event = new Event(0, title, venue, day, price, capacity, 0, true);
            Result<String> result = eventService.addEvent(event);

            if (result.isSuccess()) {
                clearFields();
                loadGroupedEvents();
            }
            showResult(result);
        } catch (Exception e) {
            statusLabel.getStyleClass().setAll("error-label");
            statusLabel.setText("Invalid input data.");
        }
    }

    @FXML private void handleModifyEvent() {
        try {
            String title = eventNameField.getText().trim();
            String venue = venueField.getText().trim();
            String day = dayChoiceBox.getValue();

            Event selected = eventService.getEventByTitleVenueDay(title, venue, day);
            if (selected == null) {
                statusLabel.getStyleClass().setAll("error-label");
                statusLabel.setText("Select a grouped event option to modify.");
                return;
            }

            selected.setTitle(title);
            selected.setVenue(venue);
            selected.setDay(day);
            selected.setPrice(Double.parseDouble(priceField.getText().trim()));
            selected.setTotalTickets(Integer.parseInt(capacityField.getText().trim()));

            Result<String> result = eventService.updateEvent(selected);
            if (result.isSuccess()) {
                loadGroupedEvents();
            }
            showResult(result);
        } catch (Exception e) {
            statusLabel.getStyleClass().setAll("error-label");
            statusLabel.setText("Invalid input data.");
        }
    }

    @FXML private void handleDeleteEvent() {
        String title = eventNameField.getText().trim();
        String venue = venueField.getText().trim();
        String day = dayChoiceBox.getValue();
        Event selected = eventService.getEventByTitleVenueDay(title, venue, day);
        if (selected == null) {
            statusLabel.getStyleClass().setAll("error-label");
            statusLabel.setText("Select a grouped event option to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete event: " + selected.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Result<String> result = eventService.deleteEvent(selected.getEventId());
                if (result.isSuccess()) {
                    loadGroupedEvents();
                }
                showResult(result);
            }
        });
    }

    @FXML private void handleEnableEvent() {
        String title = eventNameField.getText().trim();
        String venue = venueField.getText().trim();
        String day = dayChoiceBox.getValue();
        Event selected = eventService.getEventByTitleVenueDay(title, venue, day);
        if (selected == null) {
            statusLabel.getStyleClass().setAll("error-label");
            statusLabel.setText("Select a grouped event option to enable.");
            return;
        }
        Result<String> result = eventService.enableEvent(selected.getEventId());
        if (result.isSuccess()) {
            loadGroupedEvents();
        }
        showResult(result);
    }

    @FXML private void handleDisableEvent() {
        String title = eventNameField.getText().trim();
        String venue = venueField.getText().trim();
        String day = dayChoiceBox.getValue();
        Event selected = eventService.getEventByTitleVenueDay(title, venue, day);
        if (selected == null) {
            statusLabel.getStyleClass().setAll("error-label");
            statusLabel.setText("Select a grouped event option to disable.");
            return;
        }
        Result<String> result = eventService.disableEvent(selected.getEventId());
        if (result.isSuccess()) {
            loadGroupedEvents();
        }
        showResult(result);
    }


    @FXML
    private void handleViewAllOrders() {
        NavigationManager.loadScene("/view/OrdersView.fxml", "OrdersView");
    }

    @FXML
    private void handleLogout() {
        NavigationManager.logout();
    }

    /** Display a success or error message in the status label for 5 seconds. */
    private void showResult(Result<String> result) {
        showMessage(result.getData(), result.isSuccess());
    }

    /** Show a status message and clear it after five seconds. */
    private void showMessage(String message, boolean success) {
        statusLabel.getStyleClass().setAll(success ? "success-label" : "error-label");
        statusLabel.setText(message);
        if (hideMessage != null) {
            hideMessage.stop();
        }
        hideMessage = new PauseTransition(Duration.seconds(3));
        hideMessage.setOnFinished(e -> statusLabel.setText(""));
        hideMessage.play();
    }

    private void clearFields() {
        eventNameField.clear();
        venueField.clear();
        dayChoiceBox.setValue(null);
        priceField.clear();
        capacityField.clear();
    }

    private void populateFieldsFromOption(String title, String option) {
        if (option == null) return;
        String[] parts = option.split(" - ");
        if (parts.length != 2) return;
        String venue = parts[0].trim();
        String day = parts[1].trim();
        try {
            Event e = eventService.getEventByTitleVenueDay(title, venue, day);
            if (e != null) {
                eventNameField.setText(e.getTitle());
                venueField.setText(e.getVenue());
                dayChoiceBox.setValue(e.getDay());
                priceField.setText(String.valueOf(e.getPrice()));
                capacityField.setText(String.valueOf(e.getTotalTickets()));
            }
        } catch (Exception ex) {
            // ignore errors
        }
    }

    /** Configure cell factories for the detail table. */
    private void setupDetailTable() {
        detailTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        detailVenueCol.setCellValueFactory(new PropertyValueFactory<>("venue"));
        detailDayCol.setCellValueFactory(new PropertyValueFactory<>("day"));
        detailPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        detailTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalTickets"));
        detailSoldCol.setCellValueFactory(new PropertyValueFactory<>("soldTickets"));
        detailActiveCol.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    /**
     * Load the event matching the provided group title and option string into
     * the detail table. The option format is "venue - day".
     */
    private void loadEventForOption(String title, String option) {
        if (option == null) {
            groupEventsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        String[] parts = option.split(" - ");
        if (parts.length != 2) {
            groupEventsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        String venue = parts[0].trim();
        String day = parts[1].trim();

        try {
            Event e = eventService.getEventByTitleVenueDay(title, venue, day);
            if (e != null) {
                groupEventsTable.setItems(FXCollections.observableArrayList(e));
            } else {
                groupEventsTable.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            statusLabel.getStyleClass().setAll("error-label");
            statusLabel.setText("Error loading events: " + e.getMessage());
        }
    }

    /** Refresh detail table for the currently selected group and option. */
    private void loadEventForSelectedOption() {
        EventGroup sel = eventGroupTable.getSelectionModel().getSelectedItem();
        String option = optionChoiceBox.getValue();
        if (sel != null && option != null) {
            loadEventForOption(sel.getTitle(), option);
        } else {
            groupEventsTable.setItems(FXCollections.observableArrayList());
        }
    }

    /** Update details for the given group title and option string. */
    private void updateSelectedOption(String title, String option) {
        populateFieldsFromOption(title, option);
        loadEventForOption(title, option);
    }

    /** Handle option changes for whatever group is currently selected. */
    private void updateSelectedOptionForCurrentGroup(String option) {
        EventGroup group = eventGroupTable.getSelectionModel().getSelectedItem();
        if (group != null && option != null) {
            updateSelectedOption(group.getTitle(), option);
        } else {
            groupEventsTable.setItems(FXCollections.observableArrayList());
        }
    }
}
