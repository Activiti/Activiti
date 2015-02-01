package org.activiti.osgi.blueprint;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.VariableScopeElResolver;
import org.activiti.engine.impl.javax.el.ArrayELResolver;
import org.activiti.engine.impl.javax.el.BeanELResolver;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.ListELResolver;
import org.activiti.engine.impl.javax.el.MapELResolver;
import org.activiti.engine.impl.scripting.BeansResolverFactory;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.VariableScopeResolverFactory;
import org.activiti.osgi.OsgiScriptingEngines;

public class ProcessEngineFactoryWithELResolver extends ProcessEngineFactory {

  private BlueprintELResolver blueprintELResolver;
  private BlueprintContextELResolver blueprintContextELResolver;

  @Override
  public void init() throws Exception {
    ProcessEngineConfigurationImpl configImpl = (ProcessEngineConfigurationImpl) getProcessEngineConfiguration();
    configImpl.setExpressionManager(new BlueprintExpressionManager());

    List<ResolverFactory> resolverFactories = configImpl.getResolverFactories();
    if (resolverFactories == null) {
      resolverFactories = new ArrayList<ResolverFactory>();
      resolverFactories.add(new VariableScopeResolverFactory());
      resolverFactories.add(new BeansResolverFactory());
    }

    configImpl.setScriptingEngines(new OsgiScriptingEngines(
        new ScriptBindingsFactory(resolverFactories)));
    super.init();
  }

  public class BlueprintExpressionManager extends ExpressionManager {
    @Override
    protected ELResolver createElResolver(VariableScope variableScope) {
      CompositeELResolver compositeElResolver = new CompositeELResolver();
      compositeElResolver.add(new VariableScopeElResolver(variableScope));
      if (blueprintContextELResolver != null) {
        compositeElResolver.add(blueprintContextELResolver);
      }
      compositeElResolver.add(blueprintELResolver);
      compositeElResolver.add(new BeanELResolver());
      compositeElResolver.add(new ArrayELResolver());
      compositeElResolver.add(new ListELResolver());
      compositeElResolver.add(new MapELResolver());
      return compositeElResolver;
    }
  }

  public void setBlueprintELResolver(BlueprintELResolver blueprintELResolver) {
    this.blueprintELResolver = blueprintELResolver;
  }

  public void setBlueprintContextELResolver(BlueprintContextELResolver blueprintContextELResolver) {
    this.blueprintContextELResolver = blueprintContextELResolver;
  }
}
