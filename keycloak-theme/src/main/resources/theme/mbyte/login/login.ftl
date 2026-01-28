<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??) displayWide=social.displayInfo; section>
    <#if section = "header">
        <#if realm.name == "mbyte">
            <h1 class="text-center">Bienvenue sur M[iage].byte</h1>
        <#else>
            ${msg("loginTitle",(realm.displayName!''))}
        </#if>
    <#elseif section = "form">
        <div class="container-fluid">
            <div class="row justify-content-center">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <#if realm.password>
                                <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                                    <div class="mb-3">
                                        <label for="username" class="form-label"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
                                        <input tabindex="1" id="username" class="form-control" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off" />
                                    </div>
                                    <div class="mb-3">
                                        <label for="password" class="form-label">${msg("password")}</label>
                                        <input tabindex="2" id="password" class="form-control" name="password" type="password" autocomplete="off" />
                                    </div>
                                    <#if realm.rememberMe && !usernameEditDisabled??>
                                        <div class="mb-3 form-check">
                                            <input class="form-check-input" tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
                                        </div>
                                    </#if>
                                    <div class="d-grid">
                                        <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                                        <input tabindex="4" class="btn btn-primary" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                                    </div>
                                </form>
                            </#if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration">
                <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>
    <#elseif section = "socialProviders" >
        <#if social.providers??>
            <div id="kc-social-providers">
                <ul class="list-unstyled">
                    <#list social.providers as p>
                        <li><a href="${p.loginUrl}" class="btn btn-outline-secondary w-100 mb-2">${p.displayName}</a></li>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>
