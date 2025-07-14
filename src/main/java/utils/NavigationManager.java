package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import controller.*;
import service.*;
import java.io.IOException;
import java.util.*;
import config.AppConfig;

/**
 * Utility class for loading and switching JavaFX scenes.
 */
public class NavigationManager {

    private static Deque<Map.Entry<String, String>> sceneHistory = new ArrayDeque<>();
    private static String currentScenePath = null;
    private static Stage primaryStage;
    private static String currentTitle;

    private static CartService cartService;
    private static EventService eventService;
    private static OrderService orderService;
    private static UserService userService;


    /**
     * Configure the manager with the primary stage and service singletons.
     */
    public static void initialize(Stage stage, CartService cartService, OrderService orderService,
                                  UserService userService, EventService eventService) {
        primaryStage = stage;
        currentScenePath = "/view/LoginView.fxml";
        currentTitle = "LoginView";
        NavigationManager.cartService = cartService;
        NavigationManager.orderService = orderService;
        NavigationManager.userService = userService;
        NavigationManager.eventService = eventService;// Start from Login
    }

    /** Load a new scene without affecting back navigation stack. */
    public static void loadScene(String fxmlPath, String title) {
        loadScene(fxmlPath, title, false);  // Default to normal (not back navigation)
    }

    /**
     * Load a scene from FXML and optionally record history for back navigation.
     */
    public static void loadScene(String fxmlPath, String title, boolean isBackNavigation) {
        try {
            // Only push current scene if it's not back navigation and not same scene
            if (!isBackNavigation && currentScenePath != null && !currentScenePath.equals(fxmlPath)) {
                sceneHistory.push(new AbstractMap.SimpleEntry<>(currentScenePath, currentTitle));
            }
            currentScenePath = fxmlPath;
            currentTitle = title;

            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            injectDependencies(controller);

            Scene scene = new Scene(root);
            // Apply global stylesheet for consistent look
            String css = NavigationManager.class.getResource("/view/style.css").toExternalForm();
            scene.getStylesheets().add(css);

            // Fade transition for smoother scene changes
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);

            // Allow Escape key to navigate back
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    NavigationManager.goBack();
                }
            });


            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
            primaryStage.setWidth(AppConfig.WINDOW_WIDTH);
            primaryStage.setHeight(AppConfig.WINDOW_HEIGHT);
            ft.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Inject services into loaded controller instances. */
    private static void injectDependencies(Object controller) {
        if (controller instanceof CartController) {
            ((CartController) controller).setCartService(cartService);
        }
        if (controller instanceof ProfileController) {
            ((ProfileController) controller).setUserService(userService);
        }
        if (controller instanceof CheckoutController) {
            ((CheckoutController) controller).setOrderService(orderService);
        }
        if (controller instanceof LoginController) {
            ((LoginController) controller).setUserService(userService);
        }
        if (controller instanceof SignupController) {
            ((SignupController) controller).setUserService(userService);
        }
        if (controller instanceof AdminController) {
            ((AdminController) controller).setEventService(eventService);
        }
        if (controller instanceof OrdersController) {
            ((OrdersController) controller).setOrderService(orderService);
        }
        if (controller instanceof HomeController) {
            ((HomeController) controller).setCartService(cartService);
            ((HomeController) controller).setEventService(eventService);  // Inject EventService
            ((HomeController) controller).setUserService(userService);
        }
    }

    /** Navigate to the previous scene if available. */
    public static void goBack() {
        if (!sceneHistory.isEmpty()) {
            Map.Entry<String, String> previousScene = sceneHistory.pop();
            loadScene(previousScene.getKey(), previousScene.getValue(), true);
        } else {
            loadScene("/view/LoginView.fxml", "Login", true);
        }
    }

    /** Clear session and return to login screen. */
    public static void logout() {
        UserSession.setCurrentUser(null);
        sceneHistory.clear();
        currentScenePath = "/view/LoginView.fxml";
        loadScene("/view/LoginView.fxml", "Login");
    }
}
