package erick


new Toast().banana()

class Toast {
    def banana () {
        println this.getClass().getPackage().getName()
        println this.getClass().getCanonicalName()
    }
}