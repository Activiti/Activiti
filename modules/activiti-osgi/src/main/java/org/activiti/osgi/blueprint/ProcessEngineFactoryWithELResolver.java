package org.activiti.osgi.blueprint;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.VariableScopeElResolver;
import org.activiti.engine.impl.javax.el.ArrayELResolver;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.ListELResolver;
import org.activiti.engine.impl.javax.el.MapELResolver;
import org.activiti.osgi.blueprint.ProcessEngineFactory;


public class ProcessEngineFactoryWithELResolver extends ProcessEngineFactory {

    private BlueprintELResolver blueprintELResolver;

    @Override
    public void init() throws Exception {
      ((ProcessEngineConfigurationImpl) getProcessEngineConfiguration()).setExpressionManager(new BlueprintExpressionManager());
      super.init();
    }

    public class BlueprintExpressionManager extends ExpressionManager {
      @Override
      protected ELResolver createElResolver(VariableScope variableScope) {
        CompositeELResolver compositeElResolver = new CompositeELResolver();
        compositeElResolver.add(new VariableScopeElResolver(variableScope));
        compositeElResolver.add(blueprintELResolver);
        compositeElResolver.add(new ArrayELResolver());
        compositeElResolver.add(new ListELResolver());
        compositeElResolver.add(new MapELResolver());
        return compositeElResolver;
      }
    }
    
    public void setBlueprintELResolver(BlueprintELResolver blueprintELResolver) {
      this.blueprintELResolver = blueprintELResolver;
    }
}
