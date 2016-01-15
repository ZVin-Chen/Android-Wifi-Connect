package com.zvin.wificonnect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 序列化类 用于传送数据到别的进程
 * @author lixiujuan 
 *
 */
public class CMCCKeyValueList implements Serializable{

   private List<CMCCEntity> list=new ArrayList<CMCCEntity>();
   
   public List<CMCCEntity> getUpdateList(){
	   return list;
   }

   public int size(){
      if(list != null)
         return list.size();

      return 0;
   }
}