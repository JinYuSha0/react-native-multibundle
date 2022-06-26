package com.soul.rn.multibundle.constant;

public enum ComponentType {
  Common(0),
  Bootstrap(1),
  Default(2);
  private int index;

  private ComponentType(int index){
    this.index=index;
  }
  public int getIndex(){
    return this.index;
  }

  public static ComponentType getByValue(int value){
    for(ComponentType x:values()){
      if(x.getIndex()==value){
        return x;
      }
    }
    return null;
  }
}
