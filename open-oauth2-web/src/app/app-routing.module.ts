import {NgModule} from '@angular/core';
import {Routes, RouterModule } from '@angular/router';
import {RegisterResourceOwnerPage} from "./page/register-resource-owner-page/register-resource-owner.page";
import {RegisterClientPage} from "./page/register-client-page/register-client.page";
import {ResourceOwnerAuthenticationPage} from "./page/resource-owner-authentication-page/resource-owner-authentication.page";
import {ResourceOwnerScopesApprovalPage} from "./page/resource-owner-scopes-approval-page/resource-owner-scopes-approval.page";
import {NotFoundPage} from "./page/not-found-page/not-found.page";
import {AdministerAuthenticationCodePage} from "./page/administer-authentication-code-page/administer-authentication-code.page";
import {AdministerAuthenticationTokenPage} from "./page/administer-authentication-token-page/administer-authentication-token.page";
import {AdministerResourceOwnerPage} from "./page/administer-resource-owner-page/administer-resource-owner.page";
import {AdministerClientPage} from "./page/administer-client-page/administer-client.page";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HttpClientModule} from "@angular/common/http";


// primeng
import {AccordionModule } from "primeng/accordion";
import {AutoCompleteModule } from "primeng/autocomplete";
import {BreadcrumbModule } from "primeng/breadcrumb";
import {ButtonModule }from "primeng/button";
import {CarouselModule }from "primeng/carousel";
import {ChartModule }from "primeng/chart";
import {CheckboxModule }from "primeng/checkbox";
import {ChipsModule }from "primeng/chips";
import {CodeHighlighterModule }from "primeng/codehighlighter";
import {ColorPickerModule} from "primeng/colorpicker";
import {ConfirmDialogModule }from "primeng/confirmdialog";
import {ContextMenuModule }from "primeng/contextmenu";
import {DataGridModule }from "primeng/datagrid";
import {DataListModule }from "primeng/datalist";
import {DataScrollerModule }from "primeng/datascroller";
import {DialogModule }from "primeng/dialog";
import {DragDropModule }from "primeng/dragdrop";
import {DropdownModule }from "primeng/dropdown";
import {EditorModule }from "primeng/editor";
import {FieldsetModule }from "primeng/fieldset";
import {FileUploadModule }from "primeng/fileupload";
import {GalleriaModule }from "primeng/galleria";
import {GMapModule }from "primeng/gmap";
import {GrowlModule }from "primeng/growl";
import {InputMaskModule }from "primeng/inputmask";
import {InputSwitchModule }from "primeng/inputswitch";
import {InputTextareaModule }from "primeng/inputtextarea";
import {InputTextModule }from "primeng/inputtext";
import {KeyFilterModule } from "primeng/keyfilter";
import {LightboxModule }from "primeng/lightbox";
import {ListboxModule }from "primeng/listbox";
import {MegaMenuModule }from "primeng/megamenu";
import {MenubarModule }from "primeng/menubar";
import {MenuModule }from "primeng/menu";
import {MessagesModule }from "primeng/messages";
import {MessageModule} from "primeng/message";
import {MultiSelectModule }from "primeng/multiselect";
import {OrderListModule }from "primeng/orderlist";
import {OrganizationChartModule }from "primeng/organizationchart";
import {OverlayPanelModule }from "primeng/overlaypanel";
import {PaginatorModule }from "primeng/paginator";
import {PanelMenuModule }from "primeng/panelmenu";
import {PanelModule }from "primeng/panel";
import {PasswordModule }from "primeng/password";
import {PickListModule }from "primeng/picklist";
import {ProgressBarModule }from "primeng/progressbar";
import {RadioButtonModule }from "primeng/radiobutton";
import {RatingModule }from "primeng/rating";
import {FullCalendarModule }from "primeng/fullcalendar";
import {ScrollPanelModule }from "primeng/scrollpanel";
import {SelectButtonModule }from "primeng/selectbutton";
import {SharedModule }from "primeng/shared";
import {SidebarModule }from "primeng/sidebar";
import {SlideMenuModule }from "primeng/slidemenu";
import {SliderModule }from "primeng/slider";
import {SpinnerModule }from "primeng/spinner";
import {SplitButtonModule }from "primeng/splitbutton";
import {StepsModule }from "primeng/steps";
import {TabMenuModule }from "primeng/tabmenu";
import {TabViewModule }from "primeng/tabview";
import {TerminalModule }from "primeng/terminal";
import {TieredMenuModule }from "primeng/tieredmenu";
import {ToggleButtonModule } from "primeng/togglebutton";
import {ToolbarModule } from "primeng/toolbar";
import {TooltipModule } from "primeng/tooltip";
import {TreeModule } from "primeng/tree";
import {TreeTableModule } from "primeng/treetable";
import {TableModule} from "primeng/table";
import {CardModule} from "primeng/card";
import {DataViewModule} from "primeng/dataview";
import {TriStateCheckboxModule } from "primeng/tristatecheckbox";
import {ToastModule} from "primeng/toast";
import {CalendarModule} from "primeng/calendar";
import {ClientLoginPage} from "./page/client-login-page/client-login.page";
import {ClientProfilePage} from "./page/client-profile-page/client-profile.page";
import {ResourceOwnerLoginPage} from "./page/resource-owner-login-page/resource-owner-login.page";
import {ResourceOwnerProfilePage} from "./page/resource-owner-profile-page/resource-owner-profile.page";


const routes: Routes = [
  {path:'', pathMatch:"full", redirectTo: '/register-resource-owner'},
  {path:'register-resource-owner', component:RegisterResourceOwnerPage},
  {path:'register-client', component:RegisterClientPage},
  {path:'client', children:[
      {path:'login', component: ClientLoginPage},
      {path:'profile', component: ClientProfilePage},
      {path:'', pathMatch:'full', redirectTo: 'profile'}
  ]},
  {path:'resource-owner', children:[
      {path:'login', component: ResourceOwnerLoginPage},
      {path:'profile', component: ResourceOwnerProfilePage},
      {path:'', pathMatch:'full', redirectTo: 'profile'}
  ]},
  {path:'oauth2', children:[
    {path:'resource-owner-authentication', component:ResourceOwnerAuthenticationPage},
    {path:'resource-owner-scopes-approval', component:ResourceOwnerScopesApprovalPage},
  ]},
  {path:'admin', children:[
    {path:'client', component:AdministerClientPage},
    {path:'resource-owner', component: AdministerResourceOwnerPage},
    {path:'authentication-token', component: AdministerAuthenticationTokenPage},
    {path:'authentication-code', component:AdministerAuthenticationCodePage}
  ]},
  {path:'**', component:NotFoundPage}
];

@NgModule({
  declarations:[
    RegisterResourceOwnerPage,
    RegisterClientPage,
    ResourceOwnerAuthenticationPage,
    ResourceOwnerScopesApprovalPage,
    NotFoundPage,
    AdministerAuthenticationCodePage,
    AdministerAuthenticationTokenPage,
    AdministerResourceOwnerPage,
    AdministerClientPage,
    ClientLoginPage,
    ClientProfilePage,
    ResourceOwnerLoginPage,
    ResourceOwnerProfilePage,
  ],
  imports: [
    // Angular
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    RouterModule.forRoot(routes, {enableTracing:true, useHash:true}),


    // primeng
    SidebarModule,
    KeyFilterModule,
    TriStateCheckboxModule,
    TableModule,
    DataViewModule,
    ScrollPanelModule,
    CardModule,
    OrganizationChartModule,
    PanelMenuModule,
    ColorPickerModule,
    CalendarModule,
    ToastModule,
    AccordionModule,
    AutoCompleteModule,
    BreadcrumbModule,
    ButtonModule,
    CarouselModule,
    FullCalendarModule,
    ChartModule,
    CheckboxModule,
    CodeHighlighterModule,
    ConfirmDialogModule,
    SharedModule,
    ContextMenuModule,
    DataGridModule,
    DataListModule,
    DataScrollerModule,
    DialogModule,
    DragDropModule,
    DropdownModule,
    EditorModule,
    FieldsetModule,
    FileUploadModule,
    GalleriaModule,
    GMapModule,
    GrowlModule,
    InputMaskModule,
    InputSwitchModule,
    InputTextModule,
    InputTextareaModule,
    LightboxModule,
    ListboxModule,
    MegaMenuModule,
    MenuModule,
    MenubarModule,
    MessagesModule,
    MessageModule,
    MultiSelectModule,
    OrderListModule,
    OverlayPanelModule,
    PaginatorModule,
    PanelModule,
    PanelMenuModule,
    PasswordModule,
    PickListModule,
    ProgressBarModule,
    RadioButtonModule,
    RatingModule,
    SelectButtonModule,
    SlideMenuModule,
    SliderModule,
    SpinnerModule,
    SplitButtonModule,
    StepsModule,
    TabMenuModule,
    TabViewModule,
    TerminalModule,
    TieredMenuModule,
    ToggleButtonModule,
    ToolbarModule,
    TooltipModule,
    TreeModule,
    TreeTableModule,
    ChipsModule,
  ],
  exports: [
    RouterModule,
  ],
  providers: [
  ],
})
export class AppRoutingModule { }
