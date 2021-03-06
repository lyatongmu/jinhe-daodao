package com.jinhe.tss.framework.persistence.entityaop;

import java.util.List;

import org.aopalliance.intercept.MethodInvocation;

import com.jinhe.tss.framework.persistence.IDao;
import com.jinhe.tss.framework.persistence.IEntity;
import com.jinhe.tss.framework.persistence.ITreeSupportDao;
import com.jinhe.tss.util.BeanUtil;

/**
 * entity.decode值维护拦截器.
 * 
 */
public class DecodeInterceptor extends MatchByDaoMethodNameInterceptor {

	@SuppressWarnings({ "unchecked"})
    public Object invoke(MethodInvocation invocation) throws Throwable {
	    IDao<IEntity> dao = (IDao<IEntity>) invocation.getThis();
		Object[] args = invocation.getArguments();
		if(args == null || !( BeanUtil.isImplInterface(dao.getType(), IDecodable.class)) ) {
		    return invocation.proceed();
		}
		
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof IDecodable) {
                String methodName = invocation.getMethod().getName();
                IDecodable entity = (IDecodable) args[i];
                
                switch (judgeManipulateKind(methodName)) {
                case SAVE:
                case UPDATE:
                    String oldDecode = entity.getDecode();
                    
                    ((ITreeSupportDao)dao).saveDecodeableEntity(entity);
                    
                    // 移动时维护(修复)所有子节点的decode值。不包括节点自身
                    if(methodName.startsWith("move")) {
                        repairChildrenDecode(entity, oldDecode, dao);
                    }
                    
                    break;
                }
            }
        }	
        
        return invocation.proceed();
	}
	
    /**
     * 移动时维护所有子节点的decode值.不包括节点自身
     * @param entity
     * @param oldDecode
     */
    void repairChildrenDecode(IDecodable entity, String oldDecode, IDao<IEntity> dao){
        //说明为新增出来的节点，肯定没有子节点，也就没必要维护子节点了
        if(oldDecode == null)  return;
        
        String newDecode = entity.getDecode();
        if(oldDecode.equals(newDecode)) return;
 
        List<?> list = dao.getEntities("from " + entity.getClass().getName() + " o where o.decode like ?", oldDecode + "%" );
        DecodeUtil.repairSubNodeDecode(list, oldDecode, newDecode);
        dao.flush();
    }
}

	