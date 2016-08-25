main.ts

import HTTP_PROVIDERS

```
import {HTTP_PROVIDERS} from '@angular/http';
```

Append [HTTP_PROVIDERS] to bootstrap function

```
bootstrap(AppComponent, [HTTP_PROVIDERS]);
```

app.component.ts

Add/Append ResthubExampleComponent to @Component decorator

```
@Component({
	directives: [ResthubExampleComponent]
})
```