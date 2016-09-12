
#include "resthub.h"

#include <iostream>

using namespace std;

int main(int argc, char** argv) {

  Resthub resthub("http://gem-machine-a.cern.ch:8113");

  Response r;

  r = resthub.info();

#define RUN(method_inv) \
  cout << "resthub."#method_inv" response:\n"; \
  cout << resthub.method_inv.str() << endl;

  RUN(info());

  RUN(folders());

  RUN(tables("test"));


}
