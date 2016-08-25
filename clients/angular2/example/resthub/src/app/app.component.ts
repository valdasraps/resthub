import { Component } from '@angular/core';

import { ResthubExampleComponent } from './resthub/resthub-example.component'

@Component({
  moduleId: module.id,
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.css'],
  directives: [ResthubExampleComponent]
})
export class AppComponent {
  title = 'Angular2.rc5 Resthub Client Example';
}
