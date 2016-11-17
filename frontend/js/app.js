var app = angular.module('app', ['ui.router', 'ngResource']);
app.config(function($stateProvider, $httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];

    $stateProvider.state({
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
    });
}).run(function($state) {
    $state.go('customerList');
});

app.factory('Customers', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/', {}, {
        create: {
            method: 'POST'
        }
    });
}).factory('Customer', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/:email', {}, {
        update: {
            method: 'PUT'
        }
    });
}).factory('CustomerAddress', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/:email/address', {});
}).factory('Address', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/addresses/:id', {}, {
        create: {
            method: 'POST'
        },
        update: {
            method: 'PUT'
        }
    });
});

app.service('popupService', function($window) {
    this.showPopup = function(message) {
        return $window.confirm(message);
    }
});

function startSmartyStreets($scope) {
    // smartystreets auto-complete plugin
    $scope.ss = jQuery.LiveAddress({
        // Set key with requesting host's ip
        key: '22506383748684446',
        waitForStreet: true,
        target: "US",
        debug: false,
        enforceVerification: false,
        submitVerify: false,
        addresses: [{
            address1: "#address-1",
            address2: "#address-2",
            locality: "#city",
            administrative_area: "#state",
            postal_code: "#zipcode"
        }]
    });
}

function validateAddress($scope, popupService, callback) {
    var addressID = $scope.ss.getMappedAddresses()[0].id();
    $scope.ss.verify(addressID, function(response) {
        console.log("callback triggered");
        if (response.isValid()) {
            var barcode = response.raw[0].delivery_point_barcode;
            callback(barcode);
        } else {
            popupService.showPopup("please enter a valid address");
        }
    });
};

app.controller('CustomerListController', function CustomerListController($scope, $window, $state, popupService, Customer) {
    $scope.customers = Customer.get();
    $scope.deleteCustomer = function(customer) {
        if (popupService.showPopup('Really delete customer?')) {
            Customer.delete({"email": customer.email}, function() {
                $state.go('customerList', {}, { reload: true });
            });
        }
    };
}).controller('CustomerEditController', function($scope, $state, $stateParams, popupService, Customer, Address) {
    $scope.customer = Customer.get({ email: $stateParams.email }, function(c) {
        if ('address_ref' in c.items[0]) {
            $scope.address  = Address.get({ id: c.items[0].address_ref });
        }
    });
    
    startSmartyStreets($scope);

    $scope.updateCustomer = function() {
        // variables indicating whether each field has been modified
        var fname = $("#firstname").hasClass("ng-dirty");
        var lname = $("#lastname").hasClass("ng-dirty");
        var phone = $("#phonenumber").hasClass("ng-dirty");
        var line1 = $("#address-1").hasClass("ng-dirty");
        var line2 = $("#address-2").hasClass("ng-dirty");
        var city = $("#city").hasClass("ng-dirty");
        var state = $("#state").hasClass("ng-dirty");
        var zipcode = $("#zipcode").hasClass("ng-dirty");
        var addressDirty = line1 || line2 || city || state || zipcode;
        var customerDirty = fname || lname || phone || addressDirty;

        var customerUpdate = {};
        if (customerDirty) {
            var data = $scope.customer.items[0];
            if (fname) { customerUpdate.firstname = $("#firstname").val(); }
            if (lname) { customerUpdate.lastname = $("#lastname").val();   }
            if (phone) { customerUpdate.phonenumber = $("#phonenumber").val(); }
        }

        if (addressDirty) {
            validateAddress($scope, popupService, function(barcode) {

                var addressUpdate = {id: barcode};
                var addrData = $scope.address.items[0];
                addressUpdate.line1 = $("#address-1").val();
                addressUpdate.line2 = $("#address-2").val();
                addressUpdate.city = $("#city").val();
                addressUpdate.state = $("#state").val();
                addressUpdate.zipcode = $("#zipcode").val();
                
                console.log("updating address");
                console.log(addressUpdate);
                Address.update({id: barcode}, addressUpdate);
                
                customerUpdate.address_ref = barcode;

                console.log("updating customer");
                console.log(customerUpdate);
                Customer.update({email: $stateParams.email},
                                customerUpdate,
                                function() { $state.go('customerList'); });
            });
        }

        if (customerDirty && !addressDirty) {
            console.log("updating customer");
            console.log(customerUpdate);
            Customer.update({email: $stateParams.email},
                            customerUpdate,
                            function() { $state.go('customerList'); });
        }

        if (!customerDirty && !addressDirty) {
            console.log("nothing changed, not updating");
            $state.go('customerList');
        }
    };
}).controller('CustomerCreateController', function($scope, $state, popupService, Customers, Address) {
    $scope.customer = {};
    $scope.address  = {};

    // form validation
    $scope.$on('$viewContentLoaded', function() {
        $(document).ready(function() {
            $('.ui.form').form({
                on: 'blur',
                fields: {
                    email: {
                        identifier: 'email',
                        rules: [{
                            type: 'email',
                            prompt: 'please enter a valid email address'
                        }]
                    },
                    firstname: {
                        identifier: 'first-name',
                        rules: [{
                            type: 'minLength[1]',
                            prompt: 'first name is required'
                        }]
                    }
                }
            });
        });
    });

    startSmartyStreets($scope);

    $scope.addCustomer = function() {
        if (!($('.form.ui').form('is valid'))) { return; }

        var fname = $("#firstname").hasClass("ng-dirty");
        var lname = $("#lastname").hasClass("ng-dirty");
        var phone = $("#phonenumber").hasClass("ng-dirty");
        var line1 = $("#address-1").hasClass("ng-dirty");
        var line2 = $("#address-2").hasClass("ng-dirty");
        var city = $("#city").hasClass("ng-dirty");
        var state = $("#state").hasClass("ng-dirty");
        var zipcode = $("#zipcode").hasClass("ng-dirty");
        var addressDirty = line1 || line2 || city || state || zipcode;
        var customerData = { email: $("#email").val() };
        if (fname) { customerData.firstname = $("#firstname").val(); }
        if (lname) { customerData.lastname = $("#lastname").val();   }
        if (phone) { customerData.phonenumber = $("#phonenumber").val(); }

        console.log("adding customer");

        if (addressDirty) {
            validateAddress($scope, popupService, function(barcode) {
                var addressUpdate = {id: barcode};
                addressUpdate.line1 = $("#address-1").val();
                addressUpdate.line2 = $("#address-2").val();
                addressUpdate.city = $("#city").val();
                addressUpdate.state = $("#state").val();
                addressUpdate.zipcode = $("#zipcode").val();

                console.log("updating address");
                console.log(addressUpdate);
                Address.update({id: barcode}, addressUpdate);
                
                customerData.address_ref = barcode;
                console.log(customerData);
                Customers.create(customerData, function() { 
                    $state.go('customerList'); 
                });
            });
        } else {
            console.log(customerData);
            Customers.create(customerData, function() { $state.go('customerList'); });
        }


    };
}).controller('AddressListController', function($scope, $state, popupService, Address) {
    $scope.addresses = Address.get();
    $scope.deleteAddress = function(address) {
        if (popupService.showPopup('Really delete address?')) {
            Address.delete({"id": address.id}, function() {
                $state.go('addressList', {}, { reload: true });
            });
        }
    };
});
