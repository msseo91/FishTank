import 'package:fishtank/model/data_source.dart';
import 'package:fishtank/string.dart';
import 'package:fishtank/view/fishtank_view.dart';
import 'package:fishtank/viewmodel/signin_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fluttertoast/fluttertoast.dart';

class SignInPage extends StatefulWidget {
  static const String id = "signIn";

  const SignInPage({super.key});

  @override
  State createState() => SignInPageState();
}

class SignInPageState extends State<SignInPage> {
  SignInViewModel viewModel = SignInViewModel();

  final idTextController = TextEditingController();
  final passwordTextController = TextEditingController();

  @override
  void dispose() {
    idTextController.dispose();
    passwordTextController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("FishTank - Sign In"),
        ),
        body: ChangeNotifierProvider<SignInViewModel>(
            create: (BuildContext context) => viewModel,
            child: Consumer<SignInViewModel>(builder: (context, viewModel, _) {
              switch(viewModel.isSignIn.status) {
                case Status.none: return _signInView();
                case Status.complete: {
                  _goNextPage();
                  break;
                }
                case Status.error:
                  {
                    Fluttertoast.showToast(msg: viewModel.isSignIn.message ?? "Fail");
                    return _signInView();
                  }
                default: return Container();
              }

              return Container();
            })));
  }

  Widget _signInView() {
    return Padding(
      padding: const EdgeInsets.all(20.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          TextField(
            controller: idTextController,
            decoration: InputDecoration(
              hintText: Strings.signInTextId,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(10.0),
              ),
            ),
          ),
          Container(
            margin: const EdgeInsets.only(top: 10.0),
            child: TextField(
              controller: passwordTextController,
              obscureText: true,
              decoration: InputDecoration(
                hintText: Strings.signInTextPassword,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10.0),
                ),
              ),
            ),
          ),
          Container(
              width: double.infinity,
              margin: const EdgeInsets.only(top: 20.0),
              child: OutlinedButton(
                onPressed: () {
                  viewModel.signIn(idTextController.text, passwordTextController.text);
                },
                child: const Text("Sign In"),
              )),
        ],
      ),
    );
  }

  void _goNextPage() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Navigator.pushNamed(context, FishTankPage.id);
    });
  }
}
