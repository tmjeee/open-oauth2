
import {MessageService} from "primeng/components/common/messageservice";


// angular
import {BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule, Provider} from '@angular/core';
import {AppRoutingModule } from './app-routing.module';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

import {AppComponent } from './app.component';
import {ScopesEndPointService} from "./service/scopes-end-point.service";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {ConfigLoaderService} from "./service/config-loader.service";
import {RegisterResourceOwnerEndPointService} from "./service/register-resource-owner-end-point.service";
import {RegisterClientEndPointService} from "./service/register-client-end-point.service";
import {AuthenticationEndPointService} from "./service/authentication-end-point.service";
import {ScopesApprovalEndPointService} from "./service/scopes-approval-end-point.service";
import {ScopesRejectionEndPointService} from "./service/scopes-rejection-end-point.service";


export function initConfig(configLoaderService:ConfigLoaderService, httpClient:HttpClient):()=>void {
  return ()=>configLoaderService.init(httpClient);
}

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
  ],
  providers: [
    <Provider>{ provide: ScopesEndPointService, useClass: ScopesEndPointService },
    <Provider>{ provide: ConfigLoaderService, useClass:ConfigLoaderService },
    <Provider>{ provide: RegisterResourceOwnerEndPointService, useClass:RegisterResourceOwnerEndPointService },
    <Provider>{ provide: RegisterClientEndPointService, useClass: RegisterClientEndPointService },
    <Provider>{ provide: AuthenticationEndPointService, useClass: AuthenticationEndPointService },
    <Provider>{ provide: ScopesApprovalEndPointService, useClass: ScopesApprovalEndPointService },
    <Provider>{ provide: ScopesRejectionEndPointService, useClass: ScopesRejectionEndPointService },
    <Provider>{ provide: MessageService, useClass: MessageService },
    <Provider>{ provide: APP_INITIALIZER, useFactory: initConfig, multi: true, deps: [ConfigLoaderService, HttpClient] },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

