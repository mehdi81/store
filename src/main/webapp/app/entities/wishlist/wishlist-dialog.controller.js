(function() {
    'use strict';

    angular
        .module('storeApp')
        .controller('WishlistDialogController', WishlistDialogController);

    WishlistDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Wishlist', 'User'];

    function WishlistDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Wishlist, User) {
        var vm = this;

        vm.wishlist = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.users = User.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.wishlist.id !== null) {
                Wishlist.update(vm.wishlist, onSaveSuccess, onSaveError);
            } else {
                Wishlist.save(vm.wishlist, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('storeApp:wishlistUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.creationDate = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
