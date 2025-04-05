package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DOCTOR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOKNAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOKPHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Department;
import seedu.address.model.person.Email;
import seedu.address.model.person.HealthcareStaff;
import seedu.address.model.person.Name;
import seedu.address.model.person.NextOfKin;
import seedu.address.model.person.Patient;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ProviderRole;
import seedu.address.model.person.Role;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final CommandType COMMAND_TYPE = CommandType.EDIT;

    public static final String MESSAGE_USAGE = COMMAND_TYPE + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_ROLE + "ROLE TYPE] "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_DEPARTMENT + "DEPARTMENT] "
            + "[" + PREFIX_DOCTOR + "DOCTOR IN CHARGE] "
            + "[" + PREFIX_NOKNAME + "NOKNAME] "
            + "[" + PREFIX_NOKPHONE + "NOKPHONE] "
            + "Example: " + COMMAND_TYPE + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        model.setSelectedPerson(editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;
        Role updatedRole = editPersonDescriptor.getRole().orElse(personToEdit.getRole());
        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());

        if (personToEdit instanceof HealthcareStaff) {
            HealthcareStaff staffToEdit = (HealthcareStaff) personToEdit;
            ProviderRole updatedProviderRole = editPersonDescriptor.getProviderRole().orElse(staffToEdit
                    .getProviderRole());
            Department updatedDepartment = editPersonDescriptor.getDepartment().orElse(staffToEdit.getDepartment());
            return new HealthcareStaff(updatedName, updatedProviderRole, updatedDepartment,
                    updatedPhone, updatedEmail, updatedAddress, personToEdit.getRemark());
        } else if (personToEdit instanceof Patient) {
            Patient patientToEdit = (Patient) personToEdit;
            String updatedDocInCharge = editPersonDescriptor.getDocInCharge().orElse(patientToEdit.getDoctorInCharge());
            Department updatedDepartment = editPersonDescriptor.getDepartment().orElse(patientToEdit.getDepartment());
            NextOfKin updatedNok = patientToEdit.getNextofKin();
            if (editPersonDescriptor.getNokName().isPresent() || editPersonDescriptor.getNokPhone().isPresent()) {
                Name updatedNokName = editPersonDescriptor.getNokName().orElse(updatedNok.getName());
                Phone updatedNokPhone = editPersonDescriptor.getNokPhone().orElse(updatedNok.getPhone());
                System.out.println("editing");
                updatedNok = new NextOfKin(updatedNokName, updatedNokPhone);
            }
            return new Patient(updatedName, updatedPhone, updatedEmail, updatedAddress, personToEdit.getRemark(),
                    updatedDocInCharge, updatedNok, updatedDepartment);
        } else {
            return new Person(updatedRole, updatedName, updatedPhone, updatedEmail, updatedAddress,
                    personToEdit.getRemark());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Role role;
        private ProviderRole providerRole;
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Name nextOfKinName;
        private Phone nextOfKinPhone;
        private NextOfKin nextOfKin;
        private String docInCharge;
        private Department department;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setRole(toCopy.role);
            setProviderRole(toCopy.providerRole);
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setNokName(toCopy.nextOfKinName);
            setNokPhone(toCopy.nextOfKinPhone);
            setNok(toCopy.nextOfKin);
            setDocInCharge(toCopy.docInCharge);
            setDepartment(toCopy.department);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(providerRole, name, phone, email, address,
                    docInCharge, nextOfKinName, nextOfKinPhone, nextOfKin, department);
        }
        public void setRole(Role role) {
            this.role = role;
        }

        public Optional<Role> getRole() {
            return Optional.ofNullable(role);
        }

        public void setProviderRole(ProviderRole providerRole) {
            this.providerRole = providerRole;
        }

        public Optional<ProviderRole> getProviderRole() {
            return Optional.ofNullable(providerRole);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        public void setDepartment(Department department) {
            this.department = department;
        }

        public Optional<Department> getDepartment() {
            return Optional.ofNullable(department);
        }

        public void setNok(NextOfKin nextOfKin) {
            this.nextOfKin = nextOfKin;
        }

        public Optional<NextOfKin> getNok() {
            return Optional.ofNullable(nextOfKin);
        }

        public void setNokName(Name nextOfKinName) {
            this.nextOfKinName = nextOfKinName;
        }

        public Optional<Name> getNokName() {
            return Optional.ofNullable(nextOfKinName);
        }

        public void setNokPhone(Phone nextOfKinPhone) {
            this.nextOfKinPhone = nextOfKinPhone;
        }

        public Optional<Phone> getNokPhone() {
            return Optional.ofNullable(nextOfKinPhone);
        }

        public void setDocInCharge(String docInCharge) {
            this.docInCharge = docInCharge;
        }

        public Optional<String> getDocInCharge() {
            return Optional.ofNullable(docInCharge);
        }


        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(providerRole, otherEditPersonDescriptor.providerRole)
                    && Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(docInCharge, otherEditPersonDescriptor.docInCharge)
                    && Objects.equals(department, otherEditPersonDescriptor.department)
                    && Objects.equals(nextOfKinName, otherEditPersonDescriptor.nextOfKinName)
                    && Objects.equals(nextOfKinPhone, otherEditPersonDescriptor.nextOfKinPhone);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("provider role", providerRole)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("doctor in charge", docInCharge)
                    .add("nok name", nextOfKinName)
                    .add("nok phone", nextOfKinPhone)
                    .add("department", department)
                    .toString();
        }
    }
}
