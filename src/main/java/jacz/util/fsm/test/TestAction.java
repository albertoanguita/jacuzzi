package jacz.util.fsm.test;

import jacz.util.fsm.GenericFSMAction;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-mar-2010<br>
 * Last Modified: 09-mar-2010
 */
public class TestAction implements GenericFSMAction<String, Integer> {

    @Override
    public String processInput(String currentState, Integer input) throws IllegalArgumentException {
        if (currentState.compareTo("cero") == 0) {
            System.out.println("estado cero, recibo " + input + ", paso a uno");
            return "uno";
        }
        if (currentState.compareTo("uno") == 0) {
            System.out.println("estado uno, recibo " + input + ", paso a dos");
            return "dos";
        }
        if (currentState.compareTo("dos") == 0) {
            System.out.println("estado dos, recibo " + input + ", paso a tres");
            return "tres";
        } else {
            throw new IllegalArgumentException("estado " + currentState + " no reconocido");
        }
    }

    public String init() {
        System.out.println("acción inicial");
        return "cero";
    }

    @Override
    public boolean isFinalState(String state) {
        return state.compareTo("tres") == 0;
    }

    @Override
    public void stopped() {
        System.out.println("timer stopped");
    }
}
