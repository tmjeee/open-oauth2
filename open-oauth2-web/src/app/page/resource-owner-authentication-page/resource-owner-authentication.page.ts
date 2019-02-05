import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {map} from "rxjs/operators";
import {AuthenticationEndPointService, Res} from "../../service/authentication-end-point.service";
import {Message, MessageService} from "primeng/api";
import {Utils} from "../../service/utils";


@Component({
  templateUrl:'./resource-owner-authentication.page.html',
  styleUrls:['./resource-owner-authentication.page.scss']
})
export class ResourceOwnerAuthenticationPage implements OnInit {

  formGroup:FormGroup;
  formControlUsername:FormControl;
  formControlPassword:FormControl;
  transactionId:string;

  constructor(private formBuilder:FormBuilder,
              private activatedRoute:ActivatedRoute,
              private messageService:MessageService,
              private authenticationService:AuthenticationEndPointService) {
  }

  ngOnInit(): void {
    this.formControlUsername = this.formBuilder.control("", [Validators.required]);
    this.formControlPassword = this.formBuilder.control("", [Validators.required]);
    this.formGroup = this.formBuilder.group({
      "username": this.formControlUsername,
      "password": this.formControlPassword
    });
    this.transactionId = this.activatedRoute.snapshot.queryParamMap.get("transaction_id");
  }

  onSubmit(event:Event) {
    this.authenticationService.authenticate({
      transactionId: this.transactionId,
      username: this.formControlUsername.value,
      password: this.formControlPassword.value
    }).pipe(
      map((r:Res)=>{
        if (r.ok) {
          let f:HTMLFormElement = (<HTMLFormElement>document.querySelector(".form"));
          f.method = "GET";
          f.action = r.redirect_uri;
          f.submit();
        } else {
          this.messageService.add(<Message>{
            severity:'error',
            summary: 'Error',
            detail: Utils.reduceToHtmlList(r.messages)
          });
        }
      })
    ).subscribe();
  }
}
