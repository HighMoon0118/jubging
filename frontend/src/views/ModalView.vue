<template>
  <div class="modal">
    <div class="overlay" @click="$emit('close-modal')"></div>
    <div class="modal-card">
      <div class="modal-header">
        <span>{{ modalTitle }}</span>
        <font-awesome-icon class="icon" icon="times" @click="$emit('close-modal')"/>
      </div>
      <div class="modal-content">
        <slot />
      </div>
      <div class="modal-footer">

      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ModalView',
  props: {
    modalTitle: String,
  },
};
</script>

<style lang="scss" scoped>
@import "@/assets/css/common.scss";
/* Modal */
.modal,
.overlay {
  width: 100%;
  height: 100%;
  position: fixed;
  left: 0;
  top: 0;
}
.overlay {
  opacity: 0.5;
  background-color: black;
  padding: 20px;
}
.modal-card {
  border-radius: 15px;
  position: relative;
  max-width: 372px;
  margin: auto;
  padding: 0px;
  margin-top: 50px;
  background-color: white;
  min-height: 681px;
  z-index: 10;
  opacity: 1;
  box-shadow: 0px 5px 5px 5px rgba(55, 55, 55, .2);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 22px;
  border-bottom: 2px solid $primary;
  span {
    font-size: 20px;
    font-weight: bold;
  }
  .icon {
    font-size: 24px;
    cursor: pointer;
  }
}
.modal-content {
  padding: 20px 20px;
  margin: auto;
}
.modal-footer {
  height: 1px;
}
</style>