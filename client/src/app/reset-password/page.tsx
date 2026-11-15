import { Suspense } from "react";
import { resetPassword } from "@/actions/auth";
import { ActionForm } from "@/components/action-form";
import { AuthCard } from "@/components/auth-card";
import { SubmitButton } from "@/components/submit-button";
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { single } from "@/lib/format";

export default function ResetPasswordPage({ searchParams }: PageProps<"/reset-password">) {
  return (
    <AuthCard title="Choose a new password">
      <Suspense>
        <ResetForm searchParams={searchParams} />
      </Suspense>
    </AuthCard>
  );
}

async function ResetForm({ searchParams }: { searchParams: PageProps<"/reset-password">["searchParams"] }) {
  const token = single((await searchParams).token);

  return (
    <ActionForm action={resetPassword} className="flex flex-col gap-5">
      <FieldGroup className="gap-4">
        {token ? (
          <input type="hidden" name="token" value={token} />
        ) : (
          <Field>
            <FieldLabel htmlFor="token">Reset code</FieldLabel>
            <Input id="token" name="token" required className="font-mono" />
            <FieldDescription>It&apos;s in the password reset email. The link in that email fills it in for you.</FieldDescription>
          </Field>
        )}
        <Field>
          <FieldLabel htmlFor="newPassword">New password</FieldLabel>
          <Input id="newPassword" type="password" name="newPassword" required minLength={8} maxLength={72} autoComplete="new-password" />
          <FieldDescription>At least 8 characters.</FieldDescription>
        </Field>
      </FieldGroup>
      <SubmitButton size="lg" className="w-full">Change password</SubmitButton>
    </ActionForm>
  );
}
