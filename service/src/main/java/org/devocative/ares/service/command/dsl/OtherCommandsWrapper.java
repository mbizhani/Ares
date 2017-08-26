package org.devocative.ares.service.command.dsl;

import groovy.util.Proxy;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class OtherCommandsWrapper extends Proxy {
	private MainCommandDSL dsl;

	public OtherCommandsWrapper(MainCommandDSL dsl) {
		this.dsl = dsl;
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		Object[] argsArr = (Object[]) args;

		try {
			FoundMethod foundMethod = callMethodOfDSL(name, argsArr);
			if (foundMethod != null) {
				return foundMethod.method.invoke(dsl, foundMethod.finalArgs);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (argsArr.length == 1) {
			return dsl.getCommandCenter().exec(name, (Map<String, Object>) argsArr[0]);
		} else {
			return dsl.getCommandCenter().exec(name);
		}
	}

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

	private static class FoundMethod {
		private Method method;
		private Object[] finalArgs;

		public FoundMethod(Method method, Object[] finalArgs) {
			this.method = method;
			this.finalArgs = finalArgs;
		}
	}
}
