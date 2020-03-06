package org.activiti.api.runtime.model.impl;

import org.activiti.api.model.shared.model.ActivitiErrorMessage;

public class ActivitiErrorMessageImpl implements ActivitiErrorMessage {

    private int code;
    private String message;

    public ActivitiErrorMessageImpl() {
    }

    public ActivitiErrorMessageImpl(int status,
                                    String message){
        this.code = status;
        this.message = message;
    }

    @Override
    public int getCode(){
        return code;
    }

    public void setCode(int status){
        this.code = status;
    }

    @Override
    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }



}
