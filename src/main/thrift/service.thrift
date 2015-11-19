namespace java com.lvxingpai.idgen
#@namespace scala com.lvxingpai.idgen

service IdGen {
  string ping()

  i64 generate(1:string generator)

  i64 getCounter(1:string generator)

  void resetCounter(1:string generator, 2:i64 level)
}

