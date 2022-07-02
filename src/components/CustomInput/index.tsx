import React from "react";
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
    extra.type = 2;
  }
  if (thousandth) {
    extra.format = 1;
    if (!separator) {
      extra.thousands = ",";
    } else {
      extra.thousands = separator;
    }
  }
  return { ...props, ...extra };
}

export const CustomTextInput = React.forwardRef<TextInputProps, TextInput>(
  (props, ref) => {
    return <TextInput ref={ref} {...dealProps(props)} />;
  }
);
