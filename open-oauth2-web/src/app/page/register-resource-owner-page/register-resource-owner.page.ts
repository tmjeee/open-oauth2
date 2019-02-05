import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {RegisterResourceOwnerEndPointService, Res} from "../../service/register-resource-owner-end-point.service";
import {map} from "rxjs/operators";
import {Message, MessageService} from "primeng/api";
import {Utils} from "../../service/utils";


@Component({
  templateUrl:'./register-resource-owner.page.html',
  styleUrls:['./register-resource-owner.page.scss']
})
export class RegisterResourceOwnerPage {

  formGroup:FormGroup;
  formControlUsername:FormControl;
  formControlPassword:FormControl;
  formControlEmail:FormControl;
  formControlConfirmPassword:FormControl;
  formControlAutoApprove:FormControl;

  constructor(private formBuilder:FormBuilder,
              private registerResourceOwnerService:RegisterResourceOwnerEndPointService,
              private messageService:MessageService) {
    this.formControlUsername = formBuilder.control("", [Validators.required]);
    this.formControlPassword = formBuilder.control("", [Validators.required]);
    this.formControlEmail = formBuilder.control("", [Validators.required, Validators.email]);
    this.formControlConfirmPassword = formBuilder.control("", [Validators.required]);
    this.formControlAutoApprove = formBuilder.control(false);

    this.formGroup = formBuilder.group({
      "username": this.formControlUsername,
      "password": this.formControlPassword,
      "email":this.formControlEmail,
      "confirmPassword": this.formControlConfirmPassword,
      "autoApprove": this.formControlAutoApprove
    });
  }

  onSubmit(event:Event) {
    this.messageService.clear();
    this.registerResourceOwnerService.registerResourceOwner({
      username: <string>this.formControlUsername.value,
      password: <string>this.formControlPassword.value,
      email: <string> this.formControlEmail.value,
      confirmPassword: <string>this.formControlConfirmPassword.value,
      autoApprove:<boolean>this.formControlAutoApprove.value
    })
    .pipe(
      map((r:Res)=> {
        this.messageService.add(<Message>{
          severity: (r.ok ? 'success' : 'error'),
          summary: (r.ok ? 'Success' : 'Error'),
          detail: Utils.reduceToHtmlList(r.messages)
        })
      })
    ).subscribe();
  }
}
