package org.activiti.model.connector;

import java.util.List;

public class Action {

    private List<Variable> inputs;

    private List<Variable> outputs;

    public List<Variable> getInputs() {
        return inputs;
    }

    public List<Variable> getOutputs() {
        return outputs;
    }
}
