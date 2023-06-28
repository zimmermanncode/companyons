package co.zimmermann.companyon;

import lombok.NonNull;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

// import org.vaadin.artur.helpers.LaunchUtil;

// import jep.MainInterpreter;


@SpringBootApplication @Push
@Theme(themeClass = Lumo.class, variant = Lumo.DARK)
@PWA(name = "companyon", shortName = "companyon")
public class Main extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(@NonNull final String[] args) {
        // MainInterpreter.setSharedModulesArgv("companyion");
        // PythonComponent.THREAD.start();

        /* LaunchUtil.launchBrowserInDevelopmentMode( */ SpringApplication.run(Main.class, args); // );
    }
}
