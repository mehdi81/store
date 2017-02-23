(function() {
    'use strict';

    angular
        .module('storeApp')
        .controller('WishDialogController', WishDialogController);

    WishDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Wish', 'Wishlist'];

    function WishDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Wish, Wishlist) {
        var vm = this;

        vm.wish = entity;
        vm.clear = clear;
        vm.save = save;
        vm.wishlists = Wishlist.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.wish.id !== null) {
                Wish.update(vm.wish, onSaveSuccess, onSaveError);
            } else {
                Wish.save(vm.wish, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('storeApp:wishUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
