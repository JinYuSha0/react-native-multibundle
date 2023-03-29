import React, { memo } from "react";
// @ts-ignore
import TextInput from "./TextInput";
import { TextInputProps } from "react-native";

export interface CustomTextInputProps extends TextInputProps {
  onlyNumber?: boolean;
  thousandth?: boolean;
  decimal?: boolean;
  separator?: string;
}

function dealProps(props: CustomTextInputProps) {
  const { onlyNumber, thousandth, separator } = props;
  const extra: Record<string, any> = {};
  if (onlyNumber) {
    extra.myType = 1;
  }
  if (thousandth) {
    extra.myType = 2;
    if (!separator) {
      extra.thousands = ",";
    } else {
      extra.thousands = separator;
    }
  }
  return { ...props, ...extra };
}

export const CustomTextInput = memo(
  React.forwardRef<TextInputProps, TextInput>((props, ref) => {
    return <TextInput ref={ref} {...dealProps(props)} />;
  })
);
