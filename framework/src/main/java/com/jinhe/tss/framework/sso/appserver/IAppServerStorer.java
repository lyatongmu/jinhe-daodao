package com.jinhe.tss.framework.sso.appserver;

/**
 * <p>
 * 应用（App）配置信息接口
 * </p>
 * 
 * @see FileAppServerStorer
 * @see ParamAppServerStorer
 */
public interface IAppServerStorer {

    /**
     * 根据应用服务器编号获取应用服务器对象
     * 
     * @param code
     *             应用服务器代码
     * @return AppServer 应用服务器对象
     */
    AppServer getAppServer(String code);
}
