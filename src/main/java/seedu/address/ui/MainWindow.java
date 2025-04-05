package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final String LIGHT_THEME = "view/LightTheme.css";
    private static final String DARK_THEME = "view/DarkTheme.css";
    private static final String EXTENSIONS_CSS = "view/Extensions.css";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private boolean isDarkTheme;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ContentPanel contentPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    // Others
    private Person selectedPerson;


    @FXML
    private CheckMenuItem darkModeMenuItem;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane contentPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage} and {@code Logic}.
     */
    public MainWindow(Stage primaryStage, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;

        GuiSettings guiSettings = logic.getGuiSettings();
        // Configure the UI
        setWindowDefaultSize(guiSettings);
        setDefaultTheme(guiSettings);

        setAccelerators();

        helpWindow = new HelpWindow(isDarkTheme);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String getTheme() {
        return isDarkTheme ? DARK_THEME : LIGHT_THEME;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        personListPanel.setPersonSelectionListener(person -> {
            contentPanel.updateContent(person); // Update the ContentPanel with the selected person
            logic.setSelectedPerson(person);
        });

        contentPanel = new ContentPanel();
        contentPanelPlaceholder.getChildren().add(contentPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(logic.getAddressBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        logger.info("Main window initialized.");
    }

    /**
     * Sets the Default Theme based on {@code guiSettings}
     */
    private void setDefaultTheme(GuiSettings guiSettings) {
        isDarkTheme = guiSettings.isDarkTheme();
        logger.info("Initializing theme: " + (isDarkTheme ? "Dark" : "Light"));
        updateStyleSheets(isDarkTheme);
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    private void refreshUiState() {
        selectedPerson = logic.getSelectedPerson(); // Get the selected person from the model
        if (selectedPerson != null) {
            personListPanel.setSelectedPerson(selectedPerson); // Update ContentPanel with selected person's details
        }

        // Reset content panel if the AddressBook is empty
        if (logic.getAddressBook().getPersonList().isEmpty()) {
            contentPanelPlaceholder.getChildren().clear();
            contentPanel = new ContentPanel();
            contentPanelPlaceholder.getChildren().add(contentPanel.getRoot());
        }
    }

    /**
     * Update stylesheets based on parameter {@code isDarkTheme}
     */
    private void updateStyleSheets(boolean isDarkTheme) {
        ObservableList<String> stylesheets = primaryStage.getScene().getStylesheets();
        darkModeMenuItem.setSelected(isDarkTheme);
        stylesheets.clear();
        stylesheets.add(EXTENSIONS_CSS);
        if (isDarkTheme) {
            stylesheets.add(DARK_THEME);
        } else {
            stylesheets.add(LIGHT_THEME);
        }
        logger.fine("Stylesheets updated to " + (isDarkTheme ? "Dark Theme" : "Light Theme"));
    }

    /**
     * Toggle between Dark Theme and Light Theme
     */
    @FXML
    public void handleToggleTheme() {
        isDarkTheme = !isDarkTheme;
        logger.info("Toggling theme. New theme: " + (isDarkTheme ? "Dark" : "Light"));
        updateStyleSheets(isDarkTheme);
        helpWindow.updateStyleSheets(isDarkTheme);
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        logger.info("Exiting main window...");
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY(), isDarkTheme);
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            if (commandResult.isToggleTheme()) {
                handleToggleTheme();
            }

            refreshUiState();

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }
}
