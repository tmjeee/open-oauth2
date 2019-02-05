import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {Scopes, ScopesEndPointService} from "../../service/scopes-end-point.service";
import {map} from "rxjs/operators";
import {ConfigLoaderService} from "../../service/config-loader.service";
import {Res as ApprovalRes, ScopesApprovalEndPointService} from "../../service/scopes-approval-end-point.service";
import {Res as RejectionRes, ScopesRejectionEndPointService} from "../../service/scopes-rejection-end-point.service";
import {Message, MessageService} from "primeng/api";
import {Utils} from "../../service/utils";


@Component({
  templateUrl:'./resource-owner-scopes-approval.page.html',
  styleUrls:['./resource-owner-scopes-approval.page.scss']
})
export class ResourceOwnerScopesApprovalPage implements OnInit {

  formGroup:FormGroup;
  formControlScopes:FormControl;

  loading:boolean;

  transactionId:string;
  scopes:string[]=[];

  constructor(private formBuilder:FormBuilder,
              private activatedRoute:ActivatedRoute,
              private messageService:MessageService,
              private scopesEndPointService:ScopesEndPointService,
              private configLoaderService:ConfigLoaderService,
              private scopesApprovalService:ScopesApprovalEndPointService,
              private scopesRejectionService:ScopesRejectionEndPointService) {
    this.formControlScopes = formBuilder.control([], [Validators.required]);
    this.formGroup = this.formBuilder.group({
      "scopes":this.formControlScopes
    });
  }

  ngOnInit(): void {
    this.transactionId = this.getTransactionId();
    this.loadScopes();
  }

  loadScopes() {
    this.loading = true;
    this.scopesEndPointService
      .getScopes(this.transactionId)
      .pipe(
        map((scopes:Scopes)=>{
          this.scopes = scopes.scopes;
          this.loading = false;
        })
      )
      .subscribe()
  }

  private getTransactionId():string {
    return this.activatedRoute.snapshot.queryParamMap.get("transaction_id");
  }


  onApprove(event:Event) {
    this.scopesApprovalService.approve(
      this.formControlScopes.value,
      this.getTransactionId())
      .pipe(
        map((r:RejectionRes)=>{
          if (r.ok) {
            // redirect
            window.location.href=r.redirect_uri;
          } else {
            this.messageService.add(<Message>{
              severity: 'error',
              summary: 'Approval Error',
              detail: Utils.reduceToHtmlList(r.messages)
            });
          }
        })
      ).subscribe();

    /*
    let config:Config = this.configLoaderService.config;
    let post_url:string = config.oauth2Server+config.resourceOwnerScopesApprovalUrl;
    let form:HTMLFormElement = <HTMLFormElement>document.querySelector(".form");
    form.method="POST";
    form.action= post_url;

    // remove added hidden inputs (if any)
    document.querySelectorAll("[class='hidden']")
      .forEach((i:HTMLElement)=>{
        form.removeChild(i);
      });

    // transactionId
    let i:HTMLElement = document.createElement("input");
    i.setAttribute("type", "hidden");
    i.setAttribute("class", "hidden");
    i.setAttribute("name", "transaction_id");
    i.setAttribute("value", this.getTransactionId());
    form.appendChild(i);

    // scopes
    let scopes:string[] = <string[]>this.formControlScopes.value;
    scopes.forEach((scope:string)=>{
      let _i:HTMLElement = document.createElement("input");
      _i.setAttribute("type", "hidden");
      _i.setAttribute("name", "selected_scope");
      _i.setAttribute("class", "hidden");
      _i.setAttribute("value", scope);
      form.appendChild(_i);
    });

    form.submit();
    */
  }

  onReject(event:Event) {

    this.scopesRejectionService.reject(
      this.getTransactionId())
      .pipe(
        map((r:ApprovalRes)=>{
          if (r.ok) {
            // redirect
            window.location.href=r.redirect_uri;
          } else {
            this.messageService.add(<Message>{
              severity: 'error',
              summary: 'Rejection Error',
              detail: Utils.reduceToHtmlList(r.messages)
            });
          }
        })
      ).subscribe();

    /*
    let config:Config = this.configLoaderService.config;
    let post_url:string = config.oauth2Server+config.resourceOwnerScopesRejectionUrl;
    let form:HTMLFormElement = <HTMLFormElement>document.querySelector(".form");
    form.method="POST";
    form.action= post_url;

    // remove added hidden inputs (if any)
    document.querySelectorAll("[class='hidden']")
      .forEach((i:HTMLElement)=>{
        form.removeChild(i);
      });

    // transactionId
    let i:HTMLElement = document.createElement("input");
    i.setAttribute("type", "hidden");
    i.setAttribute("class", "hidden");
    i.setAttribute("name", "transaction_id");
    i.setAttribute("value", this.getTransactionId());
    form.appendChild(i);

    form.submit();
    */
  }
}
