from typing import Any, Callable, Dict, List


class UI:
    """Main proxy for interacting with a Companyon's web interface area."""

    # from com.vaadin.flow.component.html import *
    # from com.vaadin.flow.component.button import *

    #:  A Companyon's designated UI area (Java/Vaadin ``VerticalLayout``).
    view = None

    #:  A Companyon's Python code execution history.
    _python_exec_history: List[str] = None

    #:  A Companyon's singleton UI instance.
    _instance: 'UI' = None

    def __new__(cls, console_globals: Dict[str, Any] = None):
        if console_globals is None:
            if not isinstance(UI._instance, UI):
                raise RuntimeError(
                    "companyons.UI has not yet been initialized.")
            return UI._instance

        if isinstance(UI._instance, UI):
            raise RuntimeError(
                "companyons.UI has already been initialized.")

        cls._python_components = console_globals.pop(
            '_UI_python_components')
        cls._python_exec_history = console_globals.pop(
            '_UI_python_exec_history')

        cls._create_listener_wrapper = console_globals.pop(
            '_UI_create_listener_wrapper')

        return super().__new__(cls)

    def __init__(self, console_globals: Dict[str, Any] = None):
        """
        Initialize a Companyon's UI proxy with necessary Java references.

        :param console_globals:
            The initial globals from a Companyon's Java ``PythonConsole``'s
            ``jep.SubInterpreter``.
        """

        if console_globals is None:
            if not isinstance(UI._instance, UI):
                raise RuntimeError(
                    "companyons.UI has not yet been initialized.")
            return

        self.view = console_globals.pop('_UI_python_view')

    @staticmethod
    def accessor(func: Callable) -> Callable:
        """
        Decorator allowing a function to access/modify the web interface.

        :return:
            A wrapper that calls the decorated `func` in sync with the UI's
            backend loop.
        """

        def wrapper(*args, **kwargs):
            return UI._ui.accessSynchronously(lambda: func(*args, **kwargs))

        return wrapper

    #:  The registry of functions decorated with ``@UI.listener``.
    _listeners: List[Callable] = []

    @staticmethod
    def listener(func: Callable) -> Callable:
        """
        Decorator allowing a function to be used as UI event listener.

        :return:
            A wrapped Java function that picks and calls the decorated `func`
            from the ``UI._listeners`` registry.
        """

        listener_index = len(UI._listeners)
        UI._listeners.append(func)
        return UI._create_listener_wrapper.apply(
            f"UI._listeners[{listener_index}]()")

    def __getitem__(self, python_component_name):
        return UI._python_components[python_component_name]
