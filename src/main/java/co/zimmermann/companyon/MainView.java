package co.zimmermann.companyon;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@PageTitle("Main Companyon")
@Route(value = "", layout = MainLayout.class)
public class MainView extends Companyon {

    public MainView() {
        // super(new VerticalLayout(), new PythonConsole());
    }
}
