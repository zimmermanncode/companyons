from typing import Callable, List


class UI:
    # from com.vaadin.flow.component.html import *
    # from com.vaadin.flow.component.button import *

    @staticmethod
    def accessor(func: Callable) -> Callable:
        def wrapper(*args, **kwargs):
            return UI._ui.accessSynchronously(lambda: func(*args, **kwargs))

        return wrapper

    _listeners: List[Callable] = []

    @staticmethod
    def listener(func: Callable) -> Callable:
        # def wrapper(event):
        #     UI._listeners.append(__import__('functools').partial(func, event))
        #     UI._python_exec_history.add(f"UI._listeners[{len(UI._listeners) - 1}]()")

        # return wrapper

        UI._listeners.append(func)
        return UI._create_listener_wrapper.apply(f"UI._listeners[{len(UI._listeners) - 1}]()")

    def __getitem__(self, python_component_name):
        return UI._python_components[python_component_name]
