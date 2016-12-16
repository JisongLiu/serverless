var app = angular.module('app', ['ui.router', 'ngResource']);
app.config(function($stateProvider, $httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];

    $stateProvider.state({
        name: 'login',
        url: '/login',
        templateUrl: 'partials/login.html',
        controller: 'LoginPageController'
    }).state({
        name: 'customerList',
        url:'/customers',
        templateUrl:'partials/customer-list.html',
        controller:'CustomerListController'
    }).state({
        name: 'newCustomer',
        url:'/customers/new',
        templateUrl:'partials/customer-add.html',
        controller:'CustomerCreateController'
    }).state({
        name: 'editCustomer',
        url:'/customers/:email/edit',
        templateUrl:'partials/customer-edit.html',
        controller:'CustomerEditController'
    }).state({
        name: 'addressList',
        url:'/addresses',
        templateUrl: 'partials/address-list.html',
        controller: 'AddressListController'
    }).state({
        name: 'contentBrowser',
        url:'/content',
        templateUrl: 'partials/content-browser.html',
        controller: 'ContentBrowserController'
    }).state({
        name: 'contentPage',
        url: '/content/:id',
        templateUrl: 'partials/content-page.html',
        controller: 'ContentPageController'
    });
}).run(function($state) {
    $state.go('login');
});
