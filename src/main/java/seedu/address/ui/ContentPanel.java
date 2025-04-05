package seedu.address.ui;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.HealthcareStaff;
import seedu.address.model.person.Patient;
import seedu.address.model.person.Person;

/**
 * A UI component that displays the details of a selected person.
 */
public class ContentPanel extends UiPart<Region> {

    private static final String FXML = "ContentPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(ContentPanel.class);

    @FXML
    private VBox contentContainer;
    @FXML
    private Label role;
    @FXML
    private Label name;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private Label department;
    @FXML
    private Label remark;
    @FXML
    private Label providerRole;
    @FXML
    private Label doctorInCharge;
    @FXML
    private Label nextOfKin;
    @FXML
    private Label nextOfKinPhone;

    public ContentPanel() {
        super(FXML);
    }

    /**
     * Update the contentContainer to reflect {@code person} details
     */
    public void updateContent(Person person) {
        // Skip if person is null
        if (person == null) {
            return;
        }

        loadContent();

        // Default Data
        role.setText(person.getRole().toString());
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        address.setText(person.getAddress().value);

        if (person instanceof HealthcareStaff healthcareStaff) {
            // Extra Details for HealthcareStaff
            providerRole.setText("[" + healthcareStaff.getProviderRole().toString() + "]");
            department.setText(healthcareStaff.getDepartment().toString());

            providerRole.setVisible(true);
            setVisiblity(nextOfKin, false);
            setVisiblity(nextOfKinPhone, false);
            setVisiblity(doctorInCharge, false);
        }

        if (person instanceof Patient patient) {
            // Extra Details for Patient
            department.setText(patient.getDepartment().toString());
            doctorInCharge.setText(patient.getDoctorInCharge());
            nextOfKin.setText(patient.getNextofKin().getName().toString());
            nextOfKinPhone.setText(patient.getNextofKin().getPhone().toString());

            providerRole.setVisible(false);
            setVisiblity(nextOfKin, true);
            setVisiblity(nextOfKinPhone, true);
            setVisiblity(doctorInCharge, true);
        }

        remark.setText(person.getRemark().toString());

        logger.info("Selected Person: " + person);
    }

    /** Set Visibility of HBox */
    private void setVisiblity(Label label, boolean visible) {
        Parent parent = label.getParent();
        if (parent instanceof HBox hbox) {
            hbox.setVisible(visible);
            hbox.setManaged(visible);
        }
    }

    private void loadContent() {
        setVisiblity(role, true);
        setVisiblity(name, true);
        setVisiblity(phone, true);
        setVisiblity(email, true);
        setVisiblity(address, true);
        setVisiblity(department, true);
        setVisiblity(remark, true);
    }
}
