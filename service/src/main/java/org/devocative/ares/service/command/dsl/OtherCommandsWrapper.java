package org.devocative.ares.service.command.dsl;

import groovy.util.Proxy;
import org.devocative.ares.cmd.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class OtherCommandsWrapper extends Proxy {
	private static final Logger logger = LoggerFactory.getLogger(OtherCommandsWrapper.class);

	private MainCommandDSL dsl;

	// ------------------------------

	public OtherCommandsWrapper(MainCommandDSL dsl) {
		this.dsl = dsl;
	}

	// ------------------------------

	@Override
	public Object invokeMethod(String name, Object args) {
		Object[] argsArr = (Object[]) args;

		try {
			FoundMethod foundMethod = callMethodOfDSL(name, argsArr);
			if (foundMethod != null) {
				return foundMethod.method.invoke(dsl, foundMethod.finalArgs);
			}
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof CommandException) {
				throw (CommandException) e.getCause();
			} else {
				logger.error("OtherCommandsWrapper: name=[{}]", name, e);
				throw new RuntimeException(e.getCause());
			}
		} catch (Exception e) {
			logger.error("OtherCommandsWrapper: name=[{}]", name, e);
			throw new RuntimeException(e);
		}

		Object result;
		if (argsArr.length == 1) {
			if (argsArr[0] instanceof Map) {
				result = dsl.getCommandCenter().exec(name, (Map<String, Object>) argsArr[0]);
			} else {
				throw new RuntimeException("Invalid User Defined Command: " + name + " (input param is not Map)");
			}
		} else {
			result = dsl.getCommandCenter().exec(name);
		}
		logger.debug("OtherCommandsWrapper: called command: cmd=[{}] result=[{}]", name, result);
		return result;
	}

	// ------------------------------

	private FoundMethod callMethodOfDSL(String name, Object[] args) {
		Method result = null;
		Object[] finalArgs = null;

		Method[] methods = dsl.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equals(name)) {
				Parameter[] parameters = method.getParameters();

				int varArgIdx = -1;
				int paramCount = parameters.length;
				if (paramCount > 0 && parameters[paramCount - 1].isVarArgs()) {
					varArgIdx = paramCount - 1;
					paramCount--;
				}

				if (args.length >= paramCount || varArgIdx > 0) {
					finalArgs = new Object[parameters.length];
					boolean allParams = true;

					for (int i = 0; i < paramCount; i++) {
						allParams = allParams && parameters[i].getType().isAssignableFrom(args[i].getClass());
						finalArgs[i] = args[i];
					}

					if (varArgIdx >= 0) {
						Parameter varArg = parameters[varArgIdx];
						Object[] varArgParams;
						if (args.length > varArgIdx) {
							varArgParams = (Object[]) Array.newInstance(varArg.getType().getComponentType(), args.length - varArgIdx);
							for (int i = varArgIdx; i < args.length; i++) {
								allParams = allParams && varArg.getType().getComponentType().isAssignableFrom(args[i].getClass());
								varArgParams[i - varArgIdx] = args[i];
							}
						} else {
							varArgParams = (Object[]) Array.newInstance(varArg.getType().getComponentType(), 0);
						}

						finalArgs[varArgIdx] = varArgParams;
					}

					if (allParams) {
						result = method;
						break;
					}
				}
			}
		}

		if (result != null) {
			return new FoundMethod(result, finalArgs);
		}
		return null;
	}

	// ------------------------------

	private static class FoundMethod {
		private Method method;
		private Object[] finalArgs;

		public FoundMethod(Method method, Object[] finalArgs) {
			this.method = method;
			this.finalArgs = finalArgs;
		}
	}
}
