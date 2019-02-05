import {ChangeDetectionStrategy, Component} from "@angular/core";
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Message, MessageService} from "primeng/api";
import {RegisterClientEndPointService, Res} from "../../service/register-client-end-point.service";
import {map} from "rxjs/operators";
import {Utils} from "../../service/utils";


@Component({
  templateUrl:'./register-client.page.html',
  styleUrls:['./register-client.page.scss'],
})
export class RegisterClientPage {

  formGroup:FormGroup;
  formControlClientId:FormControl;
  formControlClientSecret:FormControl;
  formControlDescription:FormControl;
  formControlExpiration:FormControl;
  formControlConfidential:FormControl;
  formControlGrantTypes:FormControl;
  formControlEmail:FormControl;
  formControlScopes:FormArray;
  formControlRedirectUris:FormArray;

  constructor(private formBuilder:FormBuilder,
              private registerClientService:RegisterClientEndPointService,
              private messageService:MessageService) {

    this.formControlClientId = formBuilder.control("", [Validators.required]);
    this.formControlClientSecret = formBuilder.control("", [Validators.required]);
    this.formControlDescription = formBuilder.control("");
    this.formControlExpiration = formBuilder.control("", [Validators.required, Validators.min(300)]);
    this.formControlConfidential = formBuilder.control(false, [Validators.required]);
    this.formControlGrantTypes = formBuilder.control([], [Validators.required]);
    this.formControlEmail = formBuilder.control("", [Validators.required, Validators.email]);
    this.formControlScopes = formBuilder.array([], [Validators.required]);
    this.formControlRedirectUris = formBuilder.array([], [Validators.required]);
    this.formGroup = this.formBuilder.group({
      "clientId": this.formControlClientId,
      "clientSecret": this.formControlClientSecret,
      "description": this.formControlDescription,
      "expiration": this.formControlExpiration,
      "confidential": this.formControlConfidential,
      "grantTypes": this.formControlGrantTypes,
      "scopes": this.formControlScopes,
      "redirectUris": this.formControlRedirectUris,
      "email": this.formControlEmail
    });
    this.addRedirectUri();
  }

  onSubmit(event:Event) {
    this.messageService.clear();
    this.registerClientService.registerClient({
      client_id: <string>this.formControlClientId.value,
      client_secret: <string>this.formControlClientId.value,
      email: <string>this.formControlEmail.value,
      description: <string>this.formControlDescription.value,
      expiration: <number>this.formControlExpiration.value,
      confidential: <boolean>this.formControlConfidential.value,
      grant_type: <string[]>this.formControlGrantTypes.value,
      scope: <string[]>this.formControlScopes.controls.map((c:FormControl)=>c.value),
      redirect_uri: <string[]>this.formControlRedirectUris.controls.map((c:FormControl)=>c.value)
    })
    .pipe(
      map((r:Res)=>{
        this.messageService.add(<Message>{
          severity: (r.ok?"success":"error"),
          summary: (r.ok?'Success':'Error'),
          detail: Utils.reduceToHtmlList(r.messages)
        })
        window.scroll(0,0);
      })
    ).subscribe();
  }

  onAddChip(event:{originalEvent:Event, value:string}) {
    this.formControlScopes.markAsDirty();
    this.formControlScopes.push(this.formBuilder.control(event.value, [
      Validators.required,
      Validators.pattern("^[a-zA-Z0-9]*$")
    ]));
  }

  onRemoveChip(event:{originalEvent:Event, value:string}) {
    this.formControlScopes.markAsDirty();
    let index:number = this.formControlScopes.controls.findIndex((control:FormControl)=>control.value == event.value);
    this.formControlScopes.removeAt(index);
  }

  onBlurChip(event:{originalEvent:Event}) {
    this.formControlScopes.markAsTouched();
  }

  addRedirectUri() {
    let formControl:FormControl = this.formBuilder.control("", [
        Validators.required,
        Validators.pattern("^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*(\\.[a-z]{2,5})*(:[0-9]{1,5})?(\\/.*)?$")]);
    formControl.markAsUntouched();
    formControl.markAsPristine();
    this.formControlRedirectUris.push(formControl);
    return false;
  }

  removeRedirectUri(i:FormControl) {
    let index:number = this.formControlRedirectUris.controls.indexOf(i);
    this.formControlRedirectUris.removeAt(index);
    return false;
  }

}
