import { Component } from '@angular/core';

import { ResthubService } from './resthub.service';

@Component({
  selector: 'resthub-example',
  providers: [ResthubService],
  template: `
  <pre> {{ rh_output }} </pre>
  `
})
export class ResthubExampleComponent {
  
  rh_output = "loading...";

  constructor(private _resthub: ResthubService){ } 

   ngOnInit(){
    this.example();
  }

  example(){

      let options = {'sql' : 'SELECT * FROM wbm.fills f WHERE f.LHCFILL = :fill'};
      let params = {'fill' : 5154};

      options['params'] = params;

      this._resthub.data(options).subscribe(
      data => {
          this.rh_output = JSON.stringify(data, null, 4);
          console.log(data);
        },
      err  => console.error(err),
      () => console.log('done loading')
    );

  }

}
