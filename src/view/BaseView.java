package view;

import controller.Controller;


public abstract class BaseView {
    
    protected Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    // Métodos abstractos
    public abstract void init();
    public abstract void showMessage(String msg);
    public abstract void showErrorMessage(String errorMsg);
    public abstract void end();
}
