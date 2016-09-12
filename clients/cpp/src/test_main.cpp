
#include "resthub.h"

#include <iostream>

using namespace std;

int main(int argc, char** argv) {

  Resthub resthub("http://gem-machine-a.cern.ch:8113");

  Response r;

  r = resthub.info();

#define RUN_EX(method_inv) \
  cout << #method_inv" response:\n"; \
  cout << method_inv.str() << endl;

#define RUN(method_inv) \
  cout << "resthub."#method_inv" response:\n"; \
  cout << resthub.method_inv.str() << endl;

  RUN(info());

  RUN(folders());

  RUN(tables("gem_int2r"));

  RUN(table("gem_int2r", "c10000000000000799"));

  RUN(query("select * from gem_int2r.GEM_VFAT_CHANNELS a"));

  Query q = resthub.query("select * from gem_int2r.GEM_VFAT_CHANNELS a");
  RUN_EX(q.function("count"));

  RUN_EX(q.cache());
  RUN_EX(q.cache_delete());
  RUN_EX(q.cache());

  RUN_EX(q.csv({}, 1, 3));
  RUN_EX(q.xml({}, 1, 3));
  RUN_EX(q.json({}, 1, 3));
  RUN_EX(q.json({}, 1, 3));



  RUN(queries());

  RUN(blacklist());
  RUN(blacklist("gem_int2r"));
  RUN(blacklist("gem_int2r", "GEM_VFAT_CHANNELS"));

}
